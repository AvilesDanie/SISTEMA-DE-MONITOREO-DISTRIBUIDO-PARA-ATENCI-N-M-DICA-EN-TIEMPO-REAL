package CareNotifier.service;

import CareNotifier.config.RabbitMQConfig;
import CareNotifier.model.Notification;
import CareNotifier.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CareNotifierService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // Lista en memoria para acumular notificaciones INFO
    private final List<Notification> pendingInfoNotifications = new ArrayList<>();

    /**
     * RabbitListener que recibe todos los eventos del Exchange.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveEvent(String message) {
        try {
            // Parsear el mensaje genérico a un Map
            var jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("type").asText();
            String deviceId = jsonNode.get("deviceId").asText();
            Integer value = jsonNode.has("value") ? jsonNode.get("value").asInt() : null;
            String timestamp = jsonNode.get("timestamp").asText();

            log.info("Evento recibido: {}", message);

            // Determinar prioridad
            String priority = classifyPriority(eventType);

            // Crear notificación
            Notification notification = new Notification();
            notification.setEventType(eventType);
            notification.setRecipient("doctor@example.com");
            notification.setStatus("PENDING");
            notification.setPriority(priority);
            notification.setTimestamp(ZonedDateTime.parse(timestamp));

            // Procesar según prioridad
            switch (priority) {
                case "EMERGENCY":
                    simulateSend(notification);
                    break;
                case "WARNING":
                    simulateSend(notification);
                    break;
                case "INFO":
                    pendingInfoNotifications.add(notification);
                    log.info("Notificación INFO acumulada para envío posterior.");
                    break;
            }

            // Guardar registro en BD
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Error procesando evento: {}", e.getMessage(), e);
        }
    }

    /**
     * Clasifica prioridad según tipo de evento.
     */
    private String classifyPriority(String eventType) {
        if ("CriticalHeartRateAlert".equalsIgnoreCase(eventType) ||
                "OxygenLevelCritical".equalsIgnoreCase(eventType)) {
            return "EMERGENCY";
        } else if ("DeviceOfflineAlert".equalsIgnoreCase(eventType)) {
            return "WARNING";
        } else {
            return "INFO";
        }
    }

    /**
     * Simula el envío de la notificación (correo, sms, push).
     */
    private void simulateSend(Notification notification) {
        String body = String.format("Notificación %s para %s", notification.getEventType(), notification.getRecipient());

        try {
            log.info("Simulando envío de EMAIL: {}", body);
            log.info("Simulando envío de SMS: {}", body);
            log.info("Simulando envío de PUSH: {}", body);
            notification.setStatus("SENT");
        } catch (Exception e) {
            notification.setStatus("FAILED");
            log.error("Error simulando envío: {}", e.getMessage(), e);
        }
    }


    /**
     * Tarea programada para enviar notificaciones INFO acumuladas.
     * Corre cada 30 min.
     */
    @Scheduled(fixedRate = 15000) // 30 minutos
    public void sendPendingInfoNotifications() {
        if (pendingInfoNotifications.isEmpty()) {
            return;
        }

        log.info("Enviando {} notificaciones INFO acumuladas.", pendingInfoNotifications.size());

        List<Notification> sentNotifications = new ArrayList<>();

        for (Notification notification : pendingInfoNotifications) {
            try {
                simulateSend(notification);
                notificationRepository.save(notification);
                sentNotifications.add(notification);
            } catch (Exception e) {
                log.error("Error enviando notificación INFO acumulada: {}", e.getMessage(), e);
            }
        }

        pendingInfoNotifications.removeAll(sentNotifications);
    }
}
