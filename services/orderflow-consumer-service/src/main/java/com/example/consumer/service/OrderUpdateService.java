package com.example.consumer.service;

import com.example.consumer.model.OrderEntity;
import com.example.consumer.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderUpdateService {

  private static final Logger log = LoggerFactory.getLogger(OrderUpdateService.class);
  private final OrderRepository orderRepository;

  public OrderUpdateService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public void markProcessed(UUID id) {
    OrderEntity entity = orderRepository.findById(id)
        .orElseGet(() -> new OrderEntity(id, "CREATED", "auto-created by consumer"));

    entity.setStatus("PROCESSED");
    entity.setReason(null);
    orderRepository.save(entity);
    log.info("Order {} marked PROCESSED", id);
  }
}
