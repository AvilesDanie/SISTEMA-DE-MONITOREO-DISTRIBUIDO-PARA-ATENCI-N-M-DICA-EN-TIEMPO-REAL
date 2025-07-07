package PatientDataCollector.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "vital_signs")
@Getter
@Setter
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;

}
