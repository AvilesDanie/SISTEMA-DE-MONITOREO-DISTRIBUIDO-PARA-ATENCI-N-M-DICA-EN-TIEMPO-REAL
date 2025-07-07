package CareNotifier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String eventType;
    private String recipient;
    private String status;
    private String priority;
    private String timestamp;
}
