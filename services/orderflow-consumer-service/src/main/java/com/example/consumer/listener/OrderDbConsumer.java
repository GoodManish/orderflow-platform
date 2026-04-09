package com.example.consumer.listener;

import com.example.consumer.service.OrderUpdateService;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderDbConsumer {

  private static final Logger log = LoggerFactory.getLogger(OrderDbConsumer.class);
  private final OrderUpdateService orderUpdateService;

  public OrderDbConsumer(OrderUpdateService orderUpdateService) {
    this.orderUpdateService = orderUpdateService;
  }

  @JmsListener(destination = "${order.created.queue.name}", containerFactory = "jmsListenerContainerFactory")
  public void consume(Message message) throws Exception {
    String payload = ((TextMessage) message).getText();
    UUID orderId = UUID.fromString(payload);
    log.info("Received order.created -> {}", orderId);
    orderUpdateService.markProcessed(orderId);
  }
}
