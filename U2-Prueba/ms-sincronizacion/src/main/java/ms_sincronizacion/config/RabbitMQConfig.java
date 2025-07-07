package ms_sincronizacion.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue solicitudesReloj(){
        return QueueBuilder.durable("reloj-solicitud").build();
    }
    @Bean
    public FanoutExchange relojSincronizadoExchange() {
        return new FanoutExchange("reloj-sincronizado-exchange");
    }


}
