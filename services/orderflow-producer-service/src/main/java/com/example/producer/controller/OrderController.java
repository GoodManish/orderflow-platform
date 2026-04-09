package com.example.producer.controller;

import com.example.producer.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private ProducerService producerService;

    @PostMapping
    public String createOrder(@RequestParam String orderId) {
        producerService.send();
        return "Order sent: " + orderId;
    }
}