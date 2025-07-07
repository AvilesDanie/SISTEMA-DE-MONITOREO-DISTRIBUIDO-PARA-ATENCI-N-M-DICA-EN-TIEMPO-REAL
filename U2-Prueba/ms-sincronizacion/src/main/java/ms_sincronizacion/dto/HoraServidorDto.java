package ms_sincronizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoraServidorDto {

    private Long horaServidor;
    private Map<String, Long> diferencias;
}
