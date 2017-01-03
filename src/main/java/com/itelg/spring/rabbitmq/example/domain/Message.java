package com.itelg.spring.rabbitmq.example.domain;

import java.time.LocalDateTime;

public class Message
{
    private LocalDateTime timestamp = LocalDateTime.now();

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public String toString()
    {
        return "Message [timestamp=" + timestamp + "]";
    }
}