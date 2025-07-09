package HealthAnalyzer.service;

import HealthAnalyzer.config.RabbitMQConfig;
import HealthAnalyzer.dto.AlertEventDto;
import HealthAnalyzer.dto.NewVitalSignEvent;
import HealthAnalyzer.model.MedicalAlert;
import HealthAnalyzer.repository.MedicalAlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
public class HealthAnalyzerService {

    @Autowired
    private MedicalAlertRepository medicalAlertRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Escucha los eventos de signos vitales enviados por PatientDataCollector.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleNewVitalSignEvent(String jsonEvent) {
        try {
            NewVitalSignEvent event = objectMapper.readValue(jsonEvent, NewVitalSignEvent.class);
            log.info("Evento recibido: {}", event);

            boolean isCritical = false;
            String alertType = "";
            String message = "";

            if ("heart-rate".equalsIgnoreCase(event.getType())) {
                if (event.getValue() < 40 || event.getValue() > 140) {
                    isCritical = true;
                    alertType = "CriticalHeartRateAlert";
                    message = "Frecuencia cardíaca crítica: " + event.getValue();
                }
            }

            if ("oxygen".equalsIgnoreCase(event.getType())) {
                if (event.getValue() < 90) {
                    isCritical = true;
                    alertType = "OxygenLevelCritical";
                    message = "Nivel de oxígeno bajo: " + event.getValue() + "%";
                }
            }

            if ("blood-pressure".equalsIgnoreCase(event.getType())) {
                if (event.getValue() > 180) {
                    isCritical = true;
                    alertType = "HighBloodPressureAlert";
                    message = "Presión arterial elevada: " + event.getValue();
                }
            }

            if (isCritical) {
                // Persistir alerta
                MedicalAlert alert = new MedicalAlert();
                alert.setDeviceId(event.getDeviceId());
                alert.setAlertType(alertType);
                alert.setMessage(message);
                alert.setCreatedAt(ZonedDateTime.now());
                medicalAlertRepository.save(alert);

                log.info("Alerta guardada en base de datos: {}", alertType);

                // Crear evento de alerta
                AlertEventDto alertEvent = new AlertEventDto(
                        "ALERT-" + UUID.randomUUID(),
                        event.getDeviceId(),
                        alertType,
                        message,
                        ZonedDateTime.now().toString()
                );

                String alertJson = objectMapper.writeValueAsString(alertEvent);

                // Publicar evento de alerta
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY_ALERT,
                        alertJson
                );

                log.info("Evento de alerta enviado: {}", alertJson);
            }

        } catch (JsonProcessingException e) {
            log.error("Error al deserializar evento: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando evento: {}", e.getMessage());
        }
    }
}
