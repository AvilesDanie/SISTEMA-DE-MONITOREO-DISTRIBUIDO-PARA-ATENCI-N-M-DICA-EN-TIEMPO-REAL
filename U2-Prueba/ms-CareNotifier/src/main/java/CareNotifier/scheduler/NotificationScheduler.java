package CareNotifier.scheduler;

import CareNotifier.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationScheduler {

    @Autowired
    private NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 300000) // cada 5 minutos
    public void reportNotifications() {
        long sent = notificationRepository.countByStatus("SENT");
        long failed = notificationRepository.countByStatus("FAILED");

        log.info("=== Estado de notificaciones ===");
        log.info("Notificaciones enviadas exitosamente: {}", sent);
        log.info("Notificaciones con error: {}", failed);
    }
}
