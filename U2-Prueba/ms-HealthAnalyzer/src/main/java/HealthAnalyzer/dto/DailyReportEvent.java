package HealthAnalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class DailyReportEvent {
    private String reportId;
    private String generatedAt;
    private Map<String, Long> alertSummary;
    private Set<String> devicesInvolved;
}

