package com.itelg.spring.rabbitmq.example.listener;

import org.springframework.amqp.core.Message;

public class MessageListener implements org.springframework.amqp.core.MessageListener
{
    @Override
    public void onMessage(Message message)
    {
        throw new RuntimeException("Processing failed...");
    }
}