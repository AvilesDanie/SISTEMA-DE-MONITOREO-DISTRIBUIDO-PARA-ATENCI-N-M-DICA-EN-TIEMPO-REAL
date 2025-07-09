package CareNotifier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;

import java.time.ZonedDateTime;

@Entity
@Data
public class Notification {

    @Id
    private String notificationId;

    private String eventType;    // Ej: "AlertEvent" o "DailyReportEvent"
    private String recipient;    // Ej: "doctor@hospital.com"
    private String status;       // SENT / FAILED / PENDING
    private String priority;     // EMERGENCY / WARNING / INFO
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    private ZonedDateTime timestamp;
}

