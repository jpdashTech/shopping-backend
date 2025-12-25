package com.orderflow.api.controller;

import com.orderflow.api.domain.Product;
import com.orderflow.api.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        Product saved = productService.createProduct(product, request.inventoryQuantity());
        int inventory = productService.getInventoryQuantity(saved);
        return ResponseEntity.ok(ProductResponse.from(saved, inventory));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {
        List<ProductResponse> response = productService.getProducts().stream()
                .map(product -> ProductResponse.from(product, productService.getInventoryQuantity(product)))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        int inventory = productService.getInventoryQuantity(product);
        return ResponseEntity.ok(ProductResponse.from(product, inventory));
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull BigDecimal price,
            @Min(0) int inventoryQuantity) {
    }

    public record ProductResponse(Long id, String name, String description, BigDecimal price, int inventoryQuantity) {
        public static ProductResponse from(Product product, int inventoryQuantity) {
            return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    inventoryQuantity);
        }
    }
}
