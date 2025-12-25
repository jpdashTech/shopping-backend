package com.orderflow.api.controller;

import com.orderflow.api.domain.Order;
import com.orderflow.api.domain.OrderItem;
import com.orderflow.api.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request.items().stream()
                .map(item -> new OrderService.OrderItemRequest(item.productId(), item.quantity()))
                .toList());
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrder(id)));
    }

    public record OrderRequest(@NotEmpty List<OrderItemRequest> items) {
    }

    public record OrderItemRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record OrderResponse(Long id, String status, BigDecimal totalAmount, List<OrderItemResponse> items) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount(),
                    order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }

    public record OrderItemResponse(Long productId, int quantity, BigDecimal price) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProduct().getId(), item.getQuantity(), item.getPrice());
        }
    }
}
