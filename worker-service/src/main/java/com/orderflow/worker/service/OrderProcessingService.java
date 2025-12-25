package com.orderflow.worker.service;

import com.orderflow.worker.domain.Inventory;
import com.orderflow.worker.domain.Order;
import com.orderflow.worker.domain.OrderItem;
import com.orderflow.worker.domain.OrderStatus;
import com.orderflow.worker.domain.ProcessedEvent;
import com.orderflow.worker.repository.InventoryRepository;
import com.orderflow.worker.repository.OrderRepository;
import com.orderflow.worker.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public OrderProcessingService(OrderRepository orderRepository,
                                  InventoryRepository inventoryRepository,
                                  ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void process(OrderEventEnvelope envelope) {
        if (processedEventRepository.existsById(envelope.eventId())) {
            logger.info("Event {} already processed. Skipping.", envelope.eventId());
            return;
        }

        Order order = orderRepository.findById(envelope.payload().orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found for event"));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.FAILED) {
            processedEventRepository.save(new ProcessedEvent(envelope.eventId(), OffsetDateTime.now()));
            return;
        }

        Map<OrderItem, Inventory> affectedInventory = new HashMap<>();
        boolean inventoryAvailable = true;

        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProduct(item.getProduct())
                    .orElseThrow(() -> new IllegalStateException("Inventory missing for product"));
            if (inventory.getQuantity() < item.getQuantity()) {
                inventoryAvailable = false;
                break;
            }
            affectedInventory.put(item, inventory);
        }

        if (!inventoryAvailable) {
            order.setStatus(OrderStatus.FAILED);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);
            processedEventRepository.save(new ProcessedEvent(envelope.eventId(), OffsetDateTime.now()));
            logger.info("Order {} failed due to insufficient inventory", order.getId());
            return;
        }

        for (Map.Entry<OrderItem, Inventory> entry : affectedInventory.entrySet()) {
            Inventory inventory = entry.getValue();
            inventory.setQuantity(inventory.getQuantity() - entry.getKey().getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setStatus(OrderStatus.RESERVED);
        order.setUpdatedAt(OffsetDateTime.now());
        orderRepository.save(order);

        if (paymentFails(order.getTotalAmount())) {
            for (Map.Entry<OrderItem, Inventory> entry : affectedInventory.entrySet()) {
                Inventory inventory = entry.getValue();
                inventory.setQuantity(inventory.getQuantity() + entry.getKey().getQuantity());
                inventoryRepository.save(inventory);
            }
            order.setStatus(OrderStatus.FAILED);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);
            processedEventRepository.save(new ProcessedEvent(envelope.eventId(), OffsetDateTime.now()));
            logger.info("Order {} failed due to payment", order.getId());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(OffsetDateTime.now());
        orderRepository.save(order);

        processedEventRepository.save(new ProcessedEvent(envelope.eventId(), OffsetDateTime.now()));
        logger.info("Order {} confirmed. Notification sent.", order.getId());
    }

    private boolean paymentFails(BigDecimal totalAmount) {
        return totalAmount.compareTo(BigDecimal.valueOf(1000)) > 0;
    }
}
