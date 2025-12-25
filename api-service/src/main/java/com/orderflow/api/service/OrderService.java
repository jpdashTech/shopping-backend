package com.orderflow.api.service;

import com.orderflow.api.domain.Order;
import com.orderflow.api.domain.OrderItem;
import com.orderflow.api.domain.OrderStatus;
import com.orderflow.api.domain.Product;
import com.orderflow.api.repository.OrderRepository;
import com.orderflow.api.repository.ProductRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.orderflow.api.config.CorrelationIdFilter.MDC_KEY;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SqsPublisher sqsPublisher;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, SqsPublisher sqsPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.sqsPublisher = sqsPublisher;
    }

    @Transactional
    public Order createOrder(List<OrderItemRequest> items) {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());
        order.setCorrelationId(currentCorrelationId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : items) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setPrice(product.getPrice());
            orderItems.add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        sqsPublisher.publish(OrderEventEnvelope.orderCreated(saved));

        return saved;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(MDC_KEY);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    public record OrderItemRequest(Long productId, int quantity) {
    }

    public record OrderEventEnvelope(
            String eventId,
            String eventType,
            String correlationId,
            String createdAt,
            Payload payload) {
        public static OrderEventEnvelope orderCreated(Order order) {
            return new OrderEventEnvelope(
                    UUID.randomUUID().toString(),
                    "OrderCreated",
                    order.getCorrelationId(),
                    OffsetDateTime.now().toString(),
                    new Payload(order.getId(), order.getTotalAmount(), order.getItems().stream()
                            .map(item -> new PayloadItem(item.getProduct().getId(), item.getQuantity(), item.getPrice()))
                            .toList()));
        }

        public record Payload(Long orderId, BigDecimal totalAmount, List<PayloadItem> items) {
        }

        public record PayloadItem(Long productId, int quantity, BigDecimal price) {
        }
    }
}
