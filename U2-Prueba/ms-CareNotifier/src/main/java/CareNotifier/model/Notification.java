package CareNotifier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority; // EMERGENCY, WARNING, INFO

    @Column(nullable = false)
    private ZonedDateTime timestamp;
}
