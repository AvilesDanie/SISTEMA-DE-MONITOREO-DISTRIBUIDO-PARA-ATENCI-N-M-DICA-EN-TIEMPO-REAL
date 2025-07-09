package PatientDataCollector.service;

import PatientDataCollector.config.RabbitMQConfig;
import PatientDataCollector.dto.VitalSignsDto;
import PatientDataCollector.model.EventAudit;
import PatientDataCollector.model.VitalSigns;
import PatientDataCollector.repository.EventAuditRepository;
import PatientDataCollector.repository.VitalSignsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VitalSignsService {

    @Autowired
    private EventAuditRepository eventAuditRepository;


    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Cola en memoria para almacenar eventos pendientes
    private final ConcurrentLinkedQueue<String> pendingEvents = new ConcurrentLinkedQueue<>();

    public ResponseEntity<String> crearVitalSign(VitalSignsDto dto) {
        // Validaciones básicas
        if (dto.getDeviceId() == null || dto.getDeviceId().trim().isEmpty()) {
            return new ResponseEntity<>("El ID del dispositivo es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            return new ResponseEntity<>("El tipo de signo vital es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        if (dto.getValue() == null) {
            return new ResponseEntity<>("El valor del signo vital es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        if (dto.getTimestamp() == null || dto.getTimestamp().trim().isEmpty()) {
            return new ResponseEntity<>("La fecha y hora son obligatorias.", HttpStatus.BAD_REQUEST);
        }

        // Validaciones de rango clínico
        if ("heart-rate".equalsIgnoreCase(dto.getType())) {
            if (dto.getValue() < 30 || dto.getValue() > 200) {
                return new ResponseEntity<>("El valor de frecuencia cardíaca debe estar entre 30 y 200.", HttpStatus.BAD_REQUEST);
            }
        }

        if ("oxygen".equalsIgnoreCase(dto.getType())) {
            if (dto.getValue() < 70 || dto.getValue() > 100) {
                return new ResponseEntity<>("El valor de oxígeno debe estar entre 70 y 100.", HttpStatus.BAD_REQUEST);
            }
        }

        if ("blood-pressure".equalsIgnoreCase(dto.getType())) {
            if (dto.getValue() < 80 || dto.getValue() > 200) {
                return new ResponseEntity<>("El valor de presión arterial debe estar entre 80 y 200.", HttpStatus.BAD_REQUEST);
            }
        }


        // Conversión segura de fecha
        ZonedDateTime timestamp;
        try {
            timestamp = ZonedDateTime.parse(dto.getTimestamp());
        } catch (Exception e) {
            return new ResponseEntity<>("Formato de fecha/hora inválido. Use ISO 8601.", HttpStatus.BAD_REQUEST);
        }

        // Guardar en base de datos
        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setDeviceId(dto.getDeviceId());
        vitalSigns.setType(dto.getType());
        vitalSigns.setValue(dto.getValue());
        vitalSigns.setTimestamp(timestamp);

        VitalSigns saved = vitalSignsRepository.save(vitalSigns);

        // Construir el evento
        NewVitalSignEvent event = new NewVitalSignEvent(
                "EVT-" + UUID.randomUUID(),
                saved.getDeviceId(),
                saved.getType(),
                saved.getValue(),
                saved.getTimestamp().toString()
        );

        // Serializar evento
        String jsonEvent;
        try {
            jsonEvent = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Error al serializar el evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Persistir auditoría ANTES de enviar
        EventAudit audit = new EventAudit();
        audit.setEventId(event.getEventId());
        audit.setEventType("NewVitalSignEvent");
        audit.setPayload(jsonEvent);
        audit.setCreatedAt(ZonedDateTime.now());

        eventAuditRepository.save(audit);
        log.info("Evento auditado correctamente: {}", event.getEventId());


        // Intentar enviar evento
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    jsonEvent
            );
            log.info("Evento enviado correctamente: {}", jsonEvent);
        } catch (AmqpException e) {
            log.error("RabbitMQ no disponible. Guardando evento en cola local.");
            pendingEvents.add(jsonEvent);
        }

        return new ResponseEntity<>("Signo vital registrado. El evento será procesado.", HttpStatus.CREATED);
    }

    public ResponseEntity<List<VitalSignsDto>> obtenerHistorialPorDispositivo(String deviceId) {
        List<VitalSigns> vitalSignsList = vitalSignsRepository.findAllByDeviceId(deviceId);

        if (vitalSignsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<VitalSignsDto> dtoList = vitalSignsList.stream()
                .map(vitalSigns -> new VitalSignsDto(
                        vitalSigns.getDeviceId(),
                        vitalSigns.getType(),
                        vitalSigns.getValue(),
                        vitalSigns.getTimestamp().toString()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    /**
     * Reintento automático cada 30 segundos.
     * Reintenta hasta 3 veces por evento con backoff exponencial.
     */
    @Scheduled(fixedDelay = 30000)
    public void retryPendingEvents() {
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Intentando reenviar eventos pendientes. Cantidad: {}", pendingEvents.size());

        int size = pendingEvents.size();
        for (int i = 0; i < size; i++) {
            String jsonEvent = pendingEvents.poll();
            if (jsonEvent == null) {
                continue;
            }

            boolean sent = false;
            int attempts = 0;
            long backoff = 1000; // inicio 1 segundo

            while (!sent && attempts < 3) {
                try {
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE_NAME,
                            RabbitMQConfig.ROUTING_KEY,
                            jsonEvent
                    );
                    log.info("Evento reenviado correctamente: {}", jsonEvent);
                    sent = true;
                } catch (AmqpException e) {
                    attempts++;
                    log.warn("Reintento {} fallido. Esperando {} ms", attempts, backoff);
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ignored) {
                    }
                    backoff *= 2;
                }
            }

            if (!sent) {
                // Si después de 3 intentos sigue fallando, volver a poner en la cola
                log.error("No se pudo reenviar el evento tras 3 intentos. Guardando nuevamente en la cola.");
                pendingEvents.add(jsonEvent);
            }
        }
    }

    // Clase interna para representar el evento
    @Getter
    private static class NewVitalSignEvent {
        private String eventId;
        private String deviceId;
        private String type;
        private Integer value;
        private String timestamp;

        public NewVitalSignEvent(String eventId, String deviceId, String type, Integer value, String timestamp) {
            this.eventId = eventId;
            this.deviceId = deviceId;
            this.type = type;
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
