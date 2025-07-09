package CareNotifier.repository;

import CareNotifier.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    long countByStatus(String status);

    List<Notification> findByStatus(String status);
}
