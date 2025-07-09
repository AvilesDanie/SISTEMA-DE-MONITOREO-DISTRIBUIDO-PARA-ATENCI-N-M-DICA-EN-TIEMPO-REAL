package CareNotifier.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class DailyReportEvent {
    private String reportId;
    private String timestamp;
    private Map<String, Long> alertSummary;
    private Set<String> affectedDevices;
}
