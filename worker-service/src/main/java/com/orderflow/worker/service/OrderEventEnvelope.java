package com.orderflow.worker.service;

import java.math.BigDecimal;
import java.util.List;

public record OrderEventEnvelope(
        String eventId,
        String eventType,
        String correlationId,
        String createdAt,
        Payload payload) {

    public record Payload(Long orderId, BigDecimal totalAmount, List<PayloadItem> items) {
    }

    public record PayloadItem(Long productId, int quantity, BigDecimal price) {
    }
}
