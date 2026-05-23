package com.ngtoan.phone_store.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.service.ProductService;
import com.ngtoan.phone_store.repository.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    // 🔓 PUBLIC
    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    // 🔓 PUBLIC
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable int id) {
        return productService.getById(id);
    }

    // 🔐 ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    // 🔐 ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable int id,
                                 @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }

    // 🔐 ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        productService.delete(id);
    }


    
@GetMapping("/ping")
public String ping() {
    return "OK";
}

@GetMapping("/debug/test-count")
public long testCount() {
    return productRepository.count();
}
}
