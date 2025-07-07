package ms_sincronizacion.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import ms_sincronizacion.dto.HoraSincronizadaDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RelojPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RelojPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicarHoraSincronizada(HoraSincronizadaDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            rabbitTemplate.convertAndSend("reloj-sincronizado-exchange", "", json);
            System.out.println("Enviando hora sincronizada: " + dto.getHoraSincronizada());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}