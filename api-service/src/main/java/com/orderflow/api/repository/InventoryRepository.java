package com.orderflow.api.repository;

import com.orderflow.api.domain.Inventory;
import com.orderflow.api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
}
