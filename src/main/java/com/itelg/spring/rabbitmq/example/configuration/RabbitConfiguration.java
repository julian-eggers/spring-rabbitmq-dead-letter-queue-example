package com.itelg.spring.rabbitmq.example.configuration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

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
        TopicExchange exchange = new TopicExchange("dlx-example-exchange");
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
        rabbitTemplate.setExchange("dlx-example-exchange");
        rabbitTemplate.setRoutingKey("simple");
        return rabbitTemplate;
    }

    @Bean
    public Queue simpleQueue()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "");
        arguments.put("x-dead-letter-routing-key", "dlx-example-simple-queue-dlx");
        Queue queue = new Queue("dlx-example-simple-queue", true, false, false, arguments);
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
        Queue queue = new Queue("dlx-example-simple-queue-dlx", true, false, false, arguments);
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
        container.setErrorHandler(new ErrorHandler()
        {
            @Override
            public void handleError(Throwable throwable)
            {
                log.info("Listener failed - message rerouted to dlx (Queue: dlx-example-simple-queue-dlx)");
            }
        });
        container.setQueueNames("dlx-example-simple-queue");
        container.setMessageListener(new MessageListener()
        {
            @Override
            public void onMessage(Message arg0)
            {
                throw new RuntimeException("Processing failed...");
            }
        });
        return container;
    }
}
