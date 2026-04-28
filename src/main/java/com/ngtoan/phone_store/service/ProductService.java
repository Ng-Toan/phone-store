package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductDetailResponse;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.exception.DuplicateResourceException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.mapper.ProductMapper;
import com.ngtoan.phone_store.repository.ProductDetailRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductDetailRepository detailRepository;

    // 🔹 Lấy tất cả
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .peek(product -> product.setDetail(null))
                .toList();
    }

    // 🔹 Lấy theo ID
    public ProductResponse getById(int id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        ProductResponse response = productMapper.toResponse(product);

        detailRepository.findByProductID(id).ifPresent(detail -> {
            response.setDetail(
                    ProductDetailResponse.builder()
                            .ram(detail.getRam())
                            .storage(detail.getStorage())
                            .cpu(detail.getCpu())
                            .screen(detail.getScreen())
                            .battery(detail.getBattery())
                            .camera(detail.getCamera())
                            .os(detail.getOs())
                            .chargingSpeed(detail.getChargingSpeed())
                            .connectivity(detail.getConnectivity())
                            .build()
            );
        });

        return response;
    }

    // 🔹 Tạo mới
    public ProductResponse create(ProductRequest request) {

        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Product name already exists");
        }

        Product product = productMapper.toEntity(request);
        product.setStatus(1);

        return productMapper.toResponse(productRepository.save(product));
    }

    // 🔹 Update
    public ProductResponse update(int id, ProductUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id)
                );

        // 🔥 chỉ check nếu tên bị đổi
        if (!product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Product name already exists");
        }

        productMapper.updateProduct(product, request);
        product.setUpdatedDate(LocalDateTime.now());

        return productMapper.toResponse(productRepository.save(product));
    }

    // 🔹 Xóa
    public void delete(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id)
                );

        productRepository.delete(product);
    }
}