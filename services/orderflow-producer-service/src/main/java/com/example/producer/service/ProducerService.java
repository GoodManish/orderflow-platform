package com.example.producer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProducerService {
  private static final Logger log = LoggerFactory.getLogger(ProducerService.class);
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
  private final AtomicInteger counter = new AtomicInteger(0);
  private final JmsTemplate jmsTemplate;
  private final String queueName;
  private final String orderCreatedQueue;

  public ProducerService(JmsTemplate jmsTemplate,
                         @Value("${app.queue.name}") String queueName,
                         @Value("${order.created.queue.name}") String orderCreatedQueue) {
    this.jmsTemplate = jmsTemplate;
    this.queueName = queueName;
    this.orderCreatedQueue = orderCreatedQueue;
  }

  @Scheduled(fixedDelayString = "${app.producer.interval-ms:8000}")
  public void send() {
//    String msg = String.format("[#%04d] Hello from producer @ %s", counter.incrementAndGet(), LocalTime.now().format(FMT)); //Original
    String msg = String.format("[#%04d] SEND_TO_DLQ @ %s", counter.incrementAndGet(), LocalTime.now().format(FMT)); //Temporary
    try {
      jmsTemplate.convertAndSend(orderCreatedQueue, msg);
      log.info("SENT     -> {}", msg);
    } catch (Exception e) {
      log.warn("SEND FAILED (failover in progress?) msg={} error={}", msg, e.getMessage());
    }
  }
}
