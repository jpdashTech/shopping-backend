package com.orderflow.worker.repository;

import com.orderflow.worker.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
