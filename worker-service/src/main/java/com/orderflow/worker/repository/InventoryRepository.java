package com.orderflow.worker.repository;

import com.orderflow.worker.domain.Inventory;
import com.orderflow.worker.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
}
