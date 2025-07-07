package ms_sincronizacion.service;

import ms_sincronizacion.dto.HoraClienteDto;
import ms_sincronizacion.dto.HoraSincronizadaDto;
import ms_sincronizacion.publisher.RelojPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SincronizacionService {

    @Autowired
    private RelojPublisher relojPublisher;

    private final Map<String, Long> tiempoClientes = new ConcurrentHashMap<>();

    private static final int INTERVALO_SEGUNDOS = 10;

    public void registrarTiempoCLiente(HoraClienteDto dto){
        tiempoClientes.put(dto.getNombreNodo(), dto.getHoraEnviada());
    }

    public void sincronizarRelojes(){
        if(tiempoClientes.size() >= 2){
            long servidorHora = Instant.now().toEpochMilli();
            long promedio = (servidorHora + tiempoClientes.values().stream().mapToLong(Long::longValue).sum())
                    / (tiempoClientes.size() + 1);
            tiempoClientes.clear();
            enviarAjuste(promedio);
        }
    }


    public void enviarAjuste(long promedio) {
        HoraSincronizadaDto dto = new HoraSincronizadaDto(promedio);
        relojPublisher.publicarHoraSincronizada(dto);
    }
}
