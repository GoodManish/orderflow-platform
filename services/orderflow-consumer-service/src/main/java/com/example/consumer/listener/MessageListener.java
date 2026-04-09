package com.example.consumer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
  private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

  @JmsListener(destination = "${app.queue.name}", containerFactory = "jmsListenerContainerFactory")
  public void onMessage(String message) {
    log.info("RECEIVED <- {}", message);
  }
}
