package CareNotifier.scheduler;

import CareNotifier.controller.NotificationController;
import CareNotifier.model.Notification;
import CareNotifier.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PendingNotificationScheduler {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationController notificationController;

    @Scheduled(fixedRate = 60000) // cada 1 minuto
    public void sendPendingNotifications() {
        List<Notification> pending = notificationRepository.findByStatus("PENDING");

        if (pending.isEmpty()) {
            log.info("No hay notificaciones pendientes.");
            return;
        }

        log.info("Enviando {} notificaciones pendientes acumuladas...", pending.size());

        for (Notification notif : pending) {
            try {
                // Simular envío a canales
                log.info("[NOTIFIER] Enviando notificación acumulada: {}", notif.getPayload());
                notificationController.sendMockEmail(notif.getPayload());
                notificationController.sendMockSms(notif.getPayload());
                notificationController.sendMockPush(notif.getPayload());

                notif.setStatus("SENT");
                notificationRepository.save(notif);
            } catch (Exception e) {
                log.error("Error enviando notificación {}: {}", notif.getNotificationId(), e.getMessage());
            }
        }
    }
}
