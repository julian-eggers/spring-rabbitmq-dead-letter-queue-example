package com.itelg.spring.rabbitmq.example.configuration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.itelg.spring.actuator.rabbitmq.health.RabbitQueueCheckHealthIndicator;
import com.itelg.spring.actuator.rabbitmq.metric.configuration.EnableRabbitMetrics;

@Configuration
@EnableRabbitMetrics
public class RabbitConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(RabbitConfiguration.class);

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public MessageConverter messageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin()
    {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange dlxExampleExchange()
    {
        TopicExchange exchange = new TopicExchange("dlq-example-exchange");
        rabbitAdmin().declareExchange(exchange);
        return exchange;
    }

    /**
     * SIMPLE
     */
    @Bean
    public RabbitTemplate simpleRabbitTemplate()
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange("dlq-example-exchange");
        rabbitTemplate.setRoutingKey("simple");
        return rabbitTemplate;
    }

    @Bean
    public Queue simpleQueue()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "");
        arguments.put("x-dead-letter-routing-key", "dlq-example-simple-queue-dlq");
        Queue queue = new Queue("dlq-example-simple-queue", true, false, false, arguments);
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        rabbitAdmin().declareBinding(BindingBuilder.bind(queue).to(dlxExampleExchange()).with("simple"));
        return queue;
    }

    @Bean
    public Queue simpleQueueDlx()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-mode", "lazy");
        Queue queue = new Queue("dlq-example-simple-queue-dlq", true, false, false, arguments);
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        return queue;
    }

    @Bean
    public SimpleMessageListenerContainer simpleInboundContainer()
    {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDefaultRequeueRejected(false);
        container.setErrorHandler(throwable -> log.info("Listener failed - message rerouted to dlq (Queue: dlq-example-simple-queue-dlq)"));
        container.setQueueNames("dlq-example-simple-queue");
        container.setMessageListener((MessageListener) arg0 ->
        {
            throw new RuntimeException("Processing failed...");
        });
        return container;
    }

    @Bean
    public HealthIndicator rabbitQueueCheckHealthIndicator()
    {
        RabbitQueueCheckHealthIndicator healthIndicator = new RabbitQueueCheckHealthIndicator();
        healthIndicator.addQueueCheck(simpleQueue(), 1000, 1);
        healthIndicator.addQueueCheck(simpleQueueDlx(), 1, 0);
        return healthIndicator;
    }
}
