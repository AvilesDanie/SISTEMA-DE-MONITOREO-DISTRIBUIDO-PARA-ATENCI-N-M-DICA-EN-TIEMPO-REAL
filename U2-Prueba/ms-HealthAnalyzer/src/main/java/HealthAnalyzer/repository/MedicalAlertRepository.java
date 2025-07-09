package HealthAnalyzer.repository;

import HealthAnalyzer.model.MedicalAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalAlertRepository extends JpaRepository<MedicalAlert, Long> {
    List<MedicalAlert> findByDeviceId(String deviceId);
}
