package com.example.producer.controller;

import com.example.producer.api.CreateOrderRequest;
import com.example.producer.service.ProducerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final ProducerService producerService;

    public OrderController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public String createOrder(@RequestBody CreateOrderRequest request) {
        UUID id = producerService.createOrderAndPublish(request.orderId(), request.note());
        return "Order saved and published: " + id;
    }
}
