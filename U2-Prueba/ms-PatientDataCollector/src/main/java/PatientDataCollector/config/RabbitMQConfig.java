package PatientDataCollector.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "vitalSignsExchange";
    public static final String QUEUE_NAME = "NewVitalSignEvent";
    public static final String ROUTING_KEY = "vital.sign.new";

    // Cola
    @Bean
    public Queue newVitalSignEventQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    // Exchange (puede ser Direct o Topic; aquí uso Topic por flexibilidad)
    @Bean
    public TopicExchange vitalSignsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Binding entre cola y exchange con routing key
    @Bean
    public Binding bindingNewVitalSignEvent(Queue newVitalSignEventQueue, TopicExchange vitalSignsExchange) {
        return BindingBuilder.bind(newVitalSignEventQueue).to(vitalSignsExchange).with(ROUTING_KEY);
    }

    // Template para enviar mensajes
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
