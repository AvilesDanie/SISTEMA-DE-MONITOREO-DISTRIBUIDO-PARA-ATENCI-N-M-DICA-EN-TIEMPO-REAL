package PatientDataCollector.config;

import PatientDataCollector.dto.VitalSignsDto;
import PatientDataCollector.service.VitalSignsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.Random;

@Configuration
public class SimulatorConfig {

    private static final String[] VITAL_TYPES = {"heart-rate", "oxygen", "blood-pressure"};
    private static final Random RANDOM = new Random();

    @Bean
    public CommandLineRunner simulator(VitalSignsService service) {
        return args -> {
            for (int i = 1; i <= 5; i++) {
                String deviceId = String.format("D00%d", i);
                new Thread(createDeviceSimulator(deviceId, service)).start();
            }
        };
    }

    private Runnable createDeviceSimulator(String deviceId, VitalSignsService service) {
        return () -> {
            boolean isOnline = true;

            while (true) {
                try {
                    // Random chance to toggle online/offline state
                    if (RANDOM.nextDouble() < 0.01) {
                        isOnline = !isOnline;
                        if (isOnline) {
                            System.out.println("[SIMULATOR] Dispositivo " + deviceId + " volvió en línea.");
                        } else {
                            System.out.println("[SIMULATOR] Dispositivo " + deviceId + " se desconectó.");
                        }
                    }

                    if (isOnline) {
                        String type = pickRandomVitalSignType();
                        Integer value = generateRandomValue(type);

                        VitalSignsDto dto = new VitalSignsDto(
                                deviceId,
                                type,
                                value,
                                ZonedDateTime.now().toString()
                        );

                        service.crearVitalSign(dto);
                    }

                    Thread.sleep(10_000);
                } catch (Exception e) {
                    System.err.println("Error simulando datos del dispositivo " + deviceId + ": " + e.getMessage());
                }
            }
        };
    }

    private String pickRandomVitalSignType() {
        int idx = RANDOM.nextInt(VITAL_TYPES.length);
        return VITAL_TYPES[idx];
    }

    private Integer generateRandomValue(String type) {
        // 5% chance of generating invalid value
        boolean invalid = RANDOM.nextDouble() < 0.05;

        if ("heart-rate".equalsIgnoreCase(type)) {
            if (invalid) {
                // fuera de rango: <30 o >200
                return RANDOM.nextBoolean() ? 20 : 220;
            }
            return 50 + RANDOM.nextInt(120); // 50–170
        }

        if ("oxygen".equalsIgnoreCase(type)) {
            if (invalid) {
                return 60; // <70
            }
            return 85 + RANDOM.nextInt(15); // 85–99
        }

        if ("blood-pressure".equalsIgnoreCase(type)) {
            if (invalid) {
                return RANDOM.nextBoolean() ? 70 : 210;
            }
            return 90 + RANDOM.nextInt(50); // 90–140
        }

        return 0;
    }
}
