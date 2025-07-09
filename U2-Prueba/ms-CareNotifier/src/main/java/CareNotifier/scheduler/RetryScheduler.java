package CareNotifier.scheduler;

import CareNotifier.model.Notification;
import CareNotifier.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RetryScheduler {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000) // cada 1 minuto
    public void retryFailedNotifications() {
        List<Notification> failed = notificationRepository.findByStatus("FAILED");

        if (failed.isEmpty()) {
            log.info("No hay notificaciones pendientes de reintento.");
            return;
        }

        log.info("Reintentando {} notificaciones fallidas...", failed.size());

        for (Notification notif : failed) {
            try {
                rabbitTemplate.convertAndSend(
                        "vitalSignsExchange",
                        "vital.sign.alert",
                        notif.getPayload()
                );
                notif.setStatus("SENT");
                notificationRepository.save(notif);
                log.info("Notificación reenviada: {}", notif.getNotificationId());
            } catch (Exception e) {
                log.error("Error reenviando notificación {}: {}", notif.getNotificationId(), e.getMessage());
            }
        }
    }
}
