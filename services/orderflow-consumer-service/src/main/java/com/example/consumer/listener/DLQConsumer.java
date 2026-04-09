package com.example.consumer.listener;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;


@Component
public class DLQConsumer {

    private static final Logger log = LoggerFactory.getLogger(DLQConsumer.class);

    @JmsListener(destination = "DLQ", containerFactory = "jmsListenerContainerFactory")
    public void consumeDLQ(Message message) throws JMSException {

        String payload = ((TextMessage) message).getText();
        int deliveryCount = message.getIntProperty("JMSXDeliveryCount");

        log.error("DLQ message received: {}, deliveryAttempts: {}", payload, deliveryCount);

        // 🔹 You can:
        // - store in DB
        // - alert
        // - manual retry trigger
    }
}