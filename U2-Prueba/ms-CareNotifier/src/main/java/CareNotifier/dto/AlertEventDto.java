package CareNotifier.dto;

import lombok.Data;

@Data
public class AlertEventDto {
    private String alertId;
    private String deviceId;
    private String alertType;
    private String message;
    private String timestamp;
}
