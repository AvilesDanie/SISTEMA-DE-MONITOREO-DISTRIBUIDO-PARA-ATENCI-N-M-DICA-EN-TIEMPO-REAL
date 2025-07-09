package HealthAnalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertEventDto {
    private String alertId;
    private String deviceId;
    private String alertType;
    private String message;
    private String timestamp;
}
