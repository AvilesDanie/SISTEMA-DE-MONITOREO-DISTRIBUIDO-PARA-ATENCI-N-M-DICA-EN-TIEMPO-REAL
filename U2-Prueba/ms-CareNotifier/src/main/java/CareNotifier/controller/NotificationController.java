package CareNotifier.controller;

import CareNotifier.model.Notification;
import CareNotifier.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Obtiene todas las notificaciones registradas.
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> list = notificationRepository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * Obtiene notificaciones filtradas por tipo de evento.
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<Notification>> getByEventType(@PathVariable String eventType) {
        List<Notification> list = notificationRepository.findAll()
                .stream()
                .filter(n -> eventType.equalsIgnoreCase(n.getEventType()))
                .toList();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendMockEmail(@RequestBody String payload) {
        log.info("[MOCK EMAIL] Contenido: {}", payload);
        return ResponseEntity.ok("Email simulado enviado correctamente.");
    }

    @PostMapping("/sms")
    public ResponseEntity<String> sendMockSms(@RequestBody String payload) {
        log.info("[MOCK SMS] Contenido: {}", payload);
        return ResponseEntity.ok("SMS simulado enviado correctamente.");
    }

    @PostMapping("/push")
    public ResponseEntity<String> sendMockPush(@RequestBody String payload) {
        log.info("[MOCK PUSH] Contenido: {}", payload);
        return ResponseEntity.ok("Notificación Push simulada enviada correctamente.");
    }
}
