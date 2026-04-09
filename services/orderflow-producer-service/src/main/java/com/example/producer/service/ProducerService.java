package com.example.producer.service;

import com.example.producer.model.OrderEntity;
import com.example.producer.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProducerService {
  private static final Logger log = LoggerFactory.getLogger(ProducerService.class);
  private final JmsTemplate jmsTemplate;
  private final OrderRepository orderRepository;
  private final String orderCreatedQueue;

  public ProducerService(JmsTemplate jmsTemplate, OrderRepository orderRepository,
                         @Value("${order.created.queue.name}") String orderCreatedQueue) {
    this.jmsTemplate = jmsTemplate;
    this.orderRepository = orderRepository;
    this.orderCreatedQueue = orderCreatedQueue;
  }

  @Transactional
  public UUID createOrderAndPublish(UUID orderId, String note) {
    UUID id = orderId != null ? orderId : UUID.randomUUID();

    OrderEntity entity = new OrderEntity(id, "CREATED", note);
    orderRepository.save(entity);

    // Send lightweight payload downstream; consumer will update status.
    jmsTemplate.convertAndSend(orderCreatedQueue, id.toString());
    log.info("SENT -> orderCreatedQueue={}, orderId={}", orderCreatedQueue, id);

    return id;
  }
}
