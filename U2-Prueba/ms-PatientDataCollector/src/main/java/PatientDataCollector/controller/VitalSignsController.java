package PatientDataCollector.controller;

import PatientDataCollector.dto.VitalSignsDto;
import PatientDataCollector.service.VitalSignsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vital-signs")
public class VitalSignsController {

    @Autowired
    private VitalSignsService vitalSignsService;

    // Endpoint para crear un nuevo signo vital
    @PostMapping
    public ResponseEntity<String> crearVitalSign(@RequestBody VitalSignsDto dto) {
        return vitalSignsService.crearVitalSign(dto);
    }

    // Endpoint para obtener el historial de un dispositivo por su deviceId
    @GetMapping("/{deviceId}")
    public ResponseEntity<List<VitalSignsDto>> obtenerHistorialPorDispositivo(@PathVariable String deviceId) {
        return vitalSignsService.obtenerHistorialPorDispositivo(deviceId);
    }
}
