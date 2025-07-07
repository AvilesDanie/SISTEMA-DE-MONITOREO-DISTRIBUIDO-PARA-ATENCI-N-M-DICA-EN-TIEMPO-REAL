package CareNotifier.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MockNotificationController {

    @PostMapping("/mock-email")
    public ResponseEntity<String> simulateEmail(@RequestBody String body) {
        System.out.println("Simulación de envío EMAIL: " + body);
        return ResponseEntity.ok("Simulación de EMAIL realizada.");
    }

    @PostMapping("/mock-sms")
    public ResponseEntity<String> simulateSms(@RequestBody String body) {
        System.out.println("Simulación de envío SMS: " + body);
        return ResponseEntity.ok("Simulación de SMS realizada.");
    }

    @PostMapping("/mock-push")
    public ResponseEntity<String> simulatePush(@RequestBody String body) {
        System.out.println("Simulación de envío PUSH: " + body);
        return ResponseEntity.ok("Simulación de PUSH realizada.");
    }
}
