package com.example.producer.api;

import java.util.UUID;

public record CreateOrderRequest(UUID orderId, String note) {
}
