package HealthAnalyzer.dto;

import lombok.Data;

@Data
public class NewVitalSignEvent {
    private String eventId;
    private String deviceId;
    private String type;
    private Integer value;
    private String timestamp;
}
