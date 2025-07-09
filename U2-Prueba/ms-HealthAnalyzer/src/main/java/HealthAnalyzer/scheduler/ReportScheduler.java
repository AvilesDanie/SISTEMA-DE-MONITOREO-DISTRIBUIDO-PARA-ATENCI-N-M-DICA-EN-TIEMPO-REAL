package HealthAnalyzer.scheduler;

import HealthAnalyzer.config.RabbitMQConfig;
import HealthAnalyzer.dto.AlertEventDto;
import HealthAnalyzer.dto.DailyReportEvent;
import HealthAnalyzer.model.MedicalAlert;
import HealthAnalyzer.repository.MedicalAlertRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReportScheduler {

    @Autowired
    private MedicalAlertRepository medicalAlertRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Tarea que simula generación de un reporte diario cada 5 minutos.
     * En producción sería cada 24h.
     */
    @Scheduled(fixedRate = 300000) // cada 5 minutos
    public void generateDailyReport() {
        log.info("Generando reporte diario de alertas...");

        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);

        List<MedicalAlert> recentAlerts = medicalAlertRepository.findAll()
                .stream()
                .filter(alert -> alert.getCreatedAt().isAfter(threshold))
                .toList();

        if (recentAlerts.isEmpty()) {
            log.info("No se encontraron alertas generadas en el periodo.");
            return;
        }

        Map<String, Long> grouped = recentAlerts.stream()
                .collect(Collectors.groupingBy(MedicalAlert::getAlertType, Collectors.counting()));

        Set<String> devices = recentAlerts.stream()
                .map(MedicalAlert::getDeviceId)
                .collect(Collectors.toSet());

        log.info("Resumen de alertas en el periodo ({} alertas totales):", recentAlerts.size());
        grouped.forEach((type, count) -> log.info("- {}: {}", type, count));
        log.info("Dispositivos con alertas recientes: {}", devices);

        // CREAR EVENTO DE REPORTE
        DailyReportEvent reportEvent = new DailyReportEvent(
                "REPORT-" + UUID.randomUUID(),
                ZonedDateTime.now().toString(),
                grouped,
                devices
        );

        try {
            String json = objectMapper.writeValueAsString(reportEvent);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_REPORT, // define este routing key
                    json
            );
            log.info("Evento de reporte diario enviado: {}", json);
        } catch (Exception e) {
            log.error("Error enviando evento de reporte diario: {}", e.getMessage());
        }
    }


    /**
     * Tarea que verifica dispositivos que no han enviado datos en los últimos 5 minutos.
     * En producción sería cada 6 horas verificando si pasaron 24 horas.
     */
    @Scheduled(fixedRate = 60000) // cada 1 minuto
    public void checkInactiveDevices() {
        log.info("Verificando dispositivos inactivos...");

        // 5 minutos atrás (pruebas)
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);

        // Query: obtener deviceIds con última alerta antes del threshold
        List<Object[]> result = entityManager.createQuery(
                "SELECT DISTINCT m.deviceId, MAX(m.createdAt) " +
                        "FROM MedicalAlert m " +
                        "GROUP BY m.deviceId", Object[].class
        ).getResultList();

        // Filtrar dispositivos "viejos"
        Set<String> inactiveDevices = result.stream()
                .filter(r -> ((ZonedDateTime) r[1]).isBefore(threshold))
                .map(r -> (String) r[0])
                .collect(Collectors.toSet());

        if (inactiveDevices.isEmpty()) {
            log.info("No se encontraron dispositivos inactivos.");
            return;
        }

        log.warn("Dispositivos inactivos detectados: {}", inactiveDevices);

        for (String deviceId : inactiveDevices) {
            String alertId = "ALERT-" + UUID.randomUUID();
            String message = "Dispositivo inactivo por más de 5 minutos: " + deviceId;

            // Guardar alerta
            MedicalAlert alert = new MedicalAlert();
            alert.setDeviceId(deviceId);
            alert.setAlertType("DeviceOfflineAlert");
            alert.setMessage(message);
            alert.setCreatedAt(ZonedDateTime.now());
            medicalAlertRepository.save(alert);

            // Publicar evento
            AlertEventDto dto = new AlertEventDto(
                    alertId,
                    deviceId,
                    "DeviceOfflineAlert",
                    message,
                    ZonedDateTime.now().toString()
            );
            try {
                String json = objectMapper.writeValueAsString(dto);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY_ALERT,
                        json
                );
                log.info("Evento de dispositivo inactivo enviado: {}", json);
            } catch (Exception e) {
                log.error("Error enviando evento de inactividad: {}", e.getMessage());
            }
        }
    }
}
