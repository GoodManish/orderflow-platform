package com.example.consumer.listener;

import com.example.consumer.exception.NonRecoverableException;
import com.example.consumer.exception.RecoverableException;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;



//@Component
//public class OrderConsumer {
//
//    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);
//
//    @JmsListener(destination = "order.created", containerFactory = "jmsListenerContainerFactory")
//    public void consume(Message message) throws JMSException {
//
//        String payload = ((TextMessage) message).getText();
//        int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
//
//        log.info("Received message: {}, deliveryAttempt: {}", payload, deliveryCount);
//
//        try {
//            // 🔹 Idempotency check (important in real systems)
//            if (alreadyProcessed(payload)) {
//                log.warn("Duplicate message detected, skipping: {}", payload);
//                return;
//            }
//
//            // 🔹 Business logic
//            processOrder(payload);
//
////          log.info("Successfully processed: {}", payload);
//            log.info("RECEIVED <- {}", payload);
//
//        } catch (RecoverableException ex) {
//            log.error("Recoverable error, will retry. Attempt: {}", deliveryCount, ex);
//
//            // 👉 Throwing exception triggers Artemis retry
//            throw new RuntimeException(ex);
//
//        } catch (NonRecoverableException ex) {
//            log.error("Non-recoverable error. Sending directly to DLQ", ex);
//
//            // 👉 Option 1: Let Artemis retry and then DLQ (recommended)
//            throw new RuntimeException(ex);
//
//            // 👉 Option 2 (advanced): manually route to DLQ (skip retries)
//        }
//    }
//
//    private void processOrder(String payload) {
//        // Simulate logic
//        if (payload.contains("fail")) {
//            throw new RecoverableException("Temporary failure");
//        }
//    }
//
//    private boolean alreadyProcessed(String payload) {
//        // TODO: check DB or Redis
//        return false;
//    }
//}


//@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = "order.created", containerFactory = "jmsListenerContainerFactory")
    public void consume(Message message) throws JMSException {

        String payload = ((TextMessage) message).getText();
        int deliveryCount = message.getIntProperty("JMSXDeliveryCount");

        log.info("Received message: {}, deliveryAttempt: {}", payload, deliveryCount);

        try {

            // 🔴 1. Dummy condition to directly send to DLQ (for testing)
            if (payload.contains("SEND_TO_DLQ")) {
                log.warn("Dummy condition triggered → sending directly to DLQ");

                sendToDLQ(payload, "Manual DLQ trigger");

                return; // IMPORTANT: do not throw exception
            }

            // 🔹 Idempotency check (optional placeholder)
            if (alreadyProcessed(payload)) {
                log.warn("Duplicate message detected, skipping: {}", payload);
                return;
            }

            // 🔹 Business logic
            processOrder(payload);

            log.info("Successfully processed: {}", payload);

        } catch (NonRecoverableException ex) {

            log.error("Non-recoverable error → sending to DLQ directly", ex);

            sendToDLQ(payload, ex.getMessage());

            // DO NOT throw → skip retries
            return;

        } catch (RecoverableException ex) {

            log.error("Recoverable error → will retry via Artemis. Attempt: {}", deliveryCount, ex);

            // 👉 Let Artemis retry
            throw new RuntimeException(ex);
        }
    }

    private void processOrder(String payload) {

        // 🔴 Dummy failure scenarios

        if (payload.contains("TEMP_FAIL")) {
            throw new RecoverableException("Temporary failure");
        }

        if (payload.contains("PERM_FAIL")) {
            throw new NonRecoverableException("Permanent failure");
        }
    }

  private void sendToDLQ(String payload, String reason) {
    log.error("Sending message to DLQ. Reason: {}, Payload: {}", reason, payload);
    // Artemis default dead-letter queue is named "DLQ"
    jmsTemplate.convertAndSend("DLQ", payload);
  }

    private boolean alreadyProcessed(String payload) {
        return false;
    }
}
