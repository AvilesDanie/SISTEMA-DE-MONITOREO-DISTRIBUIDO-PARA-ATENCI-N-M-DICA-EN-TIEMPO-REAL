package PatientDataCollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import PatientDataCollector.model.VitalSigns;

import java.util.List;

public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {
    List<VitalSigns> findAllByDeviceId(String deviceId);
}
