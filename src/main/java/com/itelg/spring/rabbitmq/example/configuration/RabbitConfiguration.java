package com.itelg.spring.rabbitmq.example.configuration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.itelg.spring.actuator.rabbitmq.metric.configuration.EnableRabbitMetrics;
import com.itelg.spring.rabbitmq.example.listener.MessageListener;

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
    public TopicExchange testExchange()
    {
        TopicExchange exchange = new TopicExchange("com.itelg.spring.rabbitmq");
        rabbitAdmin().declareExchange(exchange);
        return exchange;
    }

    @Bean
    public RabbitTemplate testRabbitTemplate()
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange("com.itelg.spring.rabbitmq");
        rabbitTemplate.setRoutingKey("test");
        return rabbitTemplate;
    }

    @Bean
    public Queue testQueue()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "");
        arguments.put("x-dead-letter-routing-key", "com.itelg.spring.rabbitmq.test.dlx");
        Queue queue = new Queue("com.itelg.spring.rabbitmq.test", true, false, false, arguments);
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        rabbitAdmin().declareBinding(BindingBuilder.bind(queue).to(testExchange()).with("test"));
        return queue;
    }

    @Bean
    public SimpleMessageListenerContainer testInboundContainer()
    {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDefaultRequeueRejected(false);
        container.setErrorHandler(new ErrorHandler()
        {
            @Override
            public void handleError(Throwable throwable)
            {
                log.info("Listener failed - message rerouted to dlx (Queue: com.itelg.spring.rabbitmq.test.dlx)");
            }
        });
        container.setQueueNames("com.itelg.spring.rabbitmq.test");
        container.setMessageListener(new MessageListener());
        return container;
    }

    @Bean
    public Queue testDlxQueue()
    {
        Queue queue = new Queue("com.itelg.spring.rabbitmq.test.dlx");
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        return queue;
    }

    @Bean
    public Queue test2DlxQueue()
    {
        Queue queue = new Queue("com.itelg.spring.rabbitmq.test2.dlx");
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        return queue;
    }
}