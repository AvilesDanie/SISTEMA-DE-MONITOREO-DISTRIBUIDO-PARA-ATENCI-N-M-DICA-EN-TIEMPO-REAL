package CareNotifier.service;

import CareNotifier.config.RabbitMQConfig;
import CareNotifier.dto.AlertEventDto;
import CareNotifier.dto.DailyReportEvent;
import CareNotifier.model.Notification;
import CareNotifier.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private CareNotifier.controller.NotificationController notificationController;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Escucha eventos de alerta.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleIncomingEvent(String jsonMessage) {
        try {
            // Intentamos deserializar primero como AlertEvent
            if (jsonMessage.contains("\"alertId\"")) {
                AlertEventDto alert = objectMapper.readValue(jsonMessage, AlertEventDto.class);
                processAlertEvent(alert, jsonMessage);
            }
            // Sino intentamos como DailyReportEvent
            else if (jsonMessage.contains("\"reportId\"")) {
                DailyReportEvent report = objectMapper.readValue(jsonMessage, DailyReportEvent.class);
                processReportEvent(report, jsonMessage);
            } else {
                log.warn("Mensaje recibido no corresponde a un tipo esperado: {}", jsonMessage);
            }

        } catch (Exception e) {
            log.error("Error procesando mensaje: {}", e.getMessage());
        }
    }

    private void processAlertEvent(AlertEventDto alert, String rawJson) {
        log.info("Procesando alerta: {}", alert);

        String priority;
        switch (alert.getAlertType()) {
            case "CriticalHeartRateAlert", "DeviceOfflineAlert" -> priority = "EMERGENCY";
            case "OxygenLevelCritical" -> priority = "WARNING";
            default -> priority = "INFO";
        }

        Notification notif = new Notification();
        notif.setNotificationId("NOTIF-" + UUID.randomUUID());
        notif.setEventType("AlertEvent");
        notif.setRecipient("medico@hospital.com");
        notif.setPriority(priority);
        notif.setPayload(rawJson);
        notif.setTimestamp(ZonedDateTime.now());

        if ("EMERGENCY".equals(priority)) {
            log.info("[NOTIFIER] EMERGENCY - Enviando alerta de inmediato al personal médico: {}", alert.getMessage());

            // Simular envío a canales
            notificationController.sendMockEmail(rawJson);
            notificationController.sendMockSms(rawJson);
            notificationController.sendMockPush(rawJson);

            notif.setStatus("SENT");
        } else {
            log.info("[NOTIFIER] Alerta {} marcada como pendiente para envío agrupado.", priority);
            notif.setStatus("PENDING");
        }

        notificationRepository.save(notif);
    }


    private void processReportEvent(DailyReportEvent report, String rawJson) {
        log.info("Procesando reporte diario: {}", report);

        // Simula enviar notificación
        log.info("[NOTIFIER] Enviando reporte diario al equipo de coordinación...");

        // Persistir en base de datos
        Notification notif = new Notification();
        notif.setNotificationId("NOTIF-" + UUID.randomUUID());
        notif.setEventType("DailyReportEvent");
        notif.setRecipient("coordinador@hospital.com");
        notif.setStatus("SENT");
        notif.setPayload(rawJson);
        notif.setTimestamp(ZonedDateTime.now());
        notificationRepository.save(notif);
    }
}
