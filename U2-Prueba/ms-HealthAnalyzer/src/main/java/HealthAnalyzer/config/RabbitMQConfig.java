package HealthAnalyzer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "vitalSignsExchange";
    public static final String QUEUE_NAME = "HealthAnalyzerQueue";
    public static final String ROUTING_KEY_INPUT = "vital.sign.new";
    public static final String ROUTING_KEY_ALERT = "vital.sign.alert";
    public static final String ROUTING_KEY_REPORT = "vital.sign.report";

    // Cola que este microservicio consumirá
    @Bean
    public Queue healthAnalyzerQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }


    // Exchange compartido con los demás servicios
    @Bean
    public TopicExchange vitalSignsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Binding entre la cola principal y el exchange
    @Bean
    public Binding bindingHealthAnalyzer(Queue healthAnalyzerQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(healthAnalyzerQueue)
                .to(exchange)
                .with(ROUTING_KEY_INPUT);
    }


    // Template para publicar eventos
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
