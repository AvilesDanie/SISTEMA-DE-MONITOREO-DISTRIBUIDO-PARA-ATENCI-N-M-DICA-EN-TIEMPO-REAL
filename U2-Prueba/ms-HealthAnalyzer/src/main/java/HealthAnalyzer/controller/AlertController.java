package HealthAnalyzer.controller;

import HealthAnalyzer.model.MedicalAlert;
import HealthAnalyzer.repository.MedicalAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private MedicalAlertRepository alertRepository;

    /**
     * Obtiene todas las alertas registradas.
     */
    @GetMapping
    public ResponseEntity<List<MedicalAlert>> getAllAlerts() {
        List<MedicalAlert> list = alertRepository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * Obtiene alertas filtradas por deviceId.
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<List<MedicalAlert>> getAlertsByDevice(@PathVariable String deviceId) {
        List<MedicalAlert> list = alertRepository.findByDeviceId(deviceId);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }
}
