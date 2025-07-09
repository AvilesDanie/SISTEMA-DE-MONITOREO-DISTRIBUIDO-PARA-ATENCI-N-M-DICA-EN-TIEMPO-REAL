package PatientDataCollector.repository;

import PatientDataCollector.model.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAuditRepository extends JpaRepository<EventAudit, String> {
}
