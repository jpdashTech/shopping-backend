package com.orderflow.api.service;

import com.orderflow.api.domain.Inventory;
import com.orderflow.api.domain.Product;
import com.orderflow.api.repository.InventoryRepository;
import com.orderflow.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Product createProduct(Product product, int inventoryQuantity) {
        Product saved = productRepository.save(product);
        Inventory inventory = new Inventory();
        inventory.setProduct(saved);
        inventory.setQuantity(inventoryQuantity);
        inventoryRepository.save(inventory);
        return saved;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public int getInventoryQuantity(Product product) {
        return inventoryRepository.findByProduct(product)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
