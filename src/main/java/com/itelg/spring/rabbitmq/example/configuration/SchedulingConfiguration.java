package com.itelg.spring.rabbitmq.example.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.itelg.spring.rabbitmq.example.domain.Message;

@Configuration
@EnableScheduling
public class SchedulingConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(SchedulingConfiguration.class);

    @Autowired
    private RabbitTemplate testRabbitTemplate;

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void publish()
    {
        Message message = new Message();
        testRabbitTemplate.convertAndSend(message);
        log.info("Message published (Queue: com.itelg.spring.rabbitmq.test)");
    }
}