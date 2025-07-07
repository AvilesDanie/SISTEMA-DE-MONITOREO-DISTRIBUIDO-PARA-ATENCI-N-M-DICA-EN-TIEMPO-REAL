package CareNotifier.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "vitalSignsExchange";
    public static final String QUEUE_NAME = "CareNotifierQueue";
    public static final String ROUTING_KEY_PATTERN = "#"; // escucha todo

    @Bean
    public Queue careNotifierQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public TopicExchange vitalSignsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue careNotifierQueue, TopicExchange vitalSignsExchange) {
        return BindingBuilder.bind(careNotifierQueue)
                .to(vitalSignsExchange)
                .with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
