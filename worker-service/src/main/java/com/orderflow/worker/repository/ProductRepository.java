package com.orderflow.worker.repository;

import com.orderflow.worker.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
