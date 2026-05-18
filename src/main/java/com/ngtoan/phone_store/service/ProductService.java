package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ProductDetailRequest;
import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.dto.request.ProductUpdateRequest;
import com.ngtoan.phone_store.dto.response.ProductDetailResponse;
import com.ngtoan.phone_store.dto.response.ProductResponse;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.entity.ProductDetail;
import com.ngtoan.phone_store.exception.DuplicateResourceException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.mapper.ProductMapper;
import com.ngtoan.phone_store.repository.ProductDetailRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductDetailRepository detailRepository;

    // Lấy tất cả sản phẩm cho admin
    // Chỉ lấy sản phẩm chưa bị ẩn: status != -1
    public List<ProductResponse> getAll() {
        return productRepository.findByStatusNot(Product.STATUS_AN_SAN_PHAM)
                .stream()
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    response.setDetail(null);
                    return response;
                })
                .toList();
    }

    // Lấy theo ID
    public ProductResponse getById(int id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getStatus() != null
                && product.getStatus() == Product.STATUS_AN_SAN_PHAM) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        ProductResponse response = productMapper.toResponse(product);
        attachDetailToResponse(response, id);

        return response;
    }

    // Tạo mới sản phẩm + bắt buộc tạo ProductDetail
    @Transactional
    public ProductResponse create(ProductRequest request) {

        if (productRepository.existsByNameAndStatusNot(
        request.getName(),
        Product.STATUS_AN_SAN_PHAM
        )) {
            throw new DuplicateResourceException("Product name already exists");
        }

        validateProductStatus(request.getStatus());

        Product product = productMapper.toEntity(request);

        if (request.getStatus() == null) {
            product.setStatus(Product.STATUS_DANG_BAN);
        } else {
            product.setStatus(request.getStatus());
        }

        Product savedProduct = productRepository.save(product);

        ProductDetail detail = buildProductDetail(request.getDetail(), savedProduct);
        detailRepository.save(detail);

        ProductResponse response = productMapper.toResponse(savedProduct);
        response.setDetail(toDetailResponse(detail));

        return response;
    }

    // Update sản phẩm + bắt buộc update ProductDetail
    @Transactional
    public ProductResponse update(int id, ProductUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id)
                );

        if (product.getStatus() != null
                && product.getStatus() == Product.STATUS_AN_SAN_PHAM) {
            throw new RuntimeException("Sản phẩm đã bị ẩn, không thể cập nhật lại.");
        }

        // Chỉ check trùng tên nếu tên bị đổi
        if (request.getName() != null
                && !product.getName().equals(request.getName())
                && productRepository.existsByNameAndStatusNot(
                        request.getName(),
                        Product.STATUS_AN_SAN_PHAM
                )) {
            throw new DuplicateResourceException("Product name already exists");
        }

        validateProductStatus(request.getStatus());

        productMapper.updateProduct(product, request);

        if (product.getStatus() == null) {
            product.setStatus(Product.STATUS_DANG_BAN);
        }

        if (product.getStatus() != Product.STATUS_DANG_BAN
                && product.getStatus() != Product.STATUS_NGUNG_BAN) {
            throw new RuntimeException("Trạng thái sản phẩm không hợp lệ.");
        }

        product.setUpdatedDate(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        ProductDetail detail = detailRepository.findByProductID(id)
                .orElseGet(() -> ProductDetail.builder()
                        .product(savedProduct)
                        .build());

        updateProductDetail(detail, request.getDetail());
        detail.setProduct(savedProduct);

        ProductDetail savedDetail = detailRepository.save(detail);

        ProductResponse response = productMapper.toResponse(savedProduct);
        response.setDetail(toDetailResponse(savedDetail));

        return response;
    }

    // Xóa mềm
    // Không xóa khỏi database để tránh lỗi khóa ngoại với OrderDetail
    // Khi bấm xóa: status = -1, tức là Ẩn sản phẩm
    @Transactional
    public void delete(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id)
                );

        product.setStatus(Product.STATUS_AN_SAN_PHAM);
        product.setUpdatedDate(LocalDateTime.now());

        productRepository.save(product);
    }

    private void validateProductStatus(Integer status) {
        if (status == null) {
            return;
        }

        if (status != Product.STATUS_DANG_BAN
                && status != Product.STATUS_NGUNG_BAN) {
            throw new RuntimeException("Trạng thái sản phẩm không hợp lệ.");
        }
    }

    private ProductDetail buildProductDetail(ProductDetailRequest request, Product product) {
        return ProductDetail.builder()
                .product(product)
                .ram(request.getRam())
                .storage(request.getStorage())
                .cpu(request.getCpu())
                .screen(request.getScreen())
                .battery(request.getBattery())
                .camera(request.getCamera())
                .os(request.getOs())
                .chargingSpeed(request.getChargingSpeed())
                .connectivity(request.getConnectivity())
                .build();
    }

    private void updateProductDetail(ProductDetail detail, ProductDetailRequest request) {
        detail.setRam(request.getRam());
        detail.setStorage(request.getStorage());
        detail.setCpu(request.getCpu());
        detail.setScreen(request.getScreen());
        detail.setBattery(request.getBattery());
        detail.setCamera(request.getCamera());
        detail.setOs(request.getOs());
        detail.setChargingSpeed(request.getChargingSpeed());
        detail.setConnectivity(request.getConnectivity());
    }

    private void attachDetailToResponse(ProductResponse response, Integer productID) {
        detailRepository.findByProductID(productID).ifPresent(detail ->
                response.setDetail(toDetailResponse(detail))
        );
    }

    private ProductDetailResponse toDetailResponse(ProductDetail detail) {
        return ProductDetailResponse.builder()
                .ram(detail.getRam())
                .storage(detail.getStorage())
                .cpu(detail.getCpu())
                .screen(detail.getScreen())
                .battery(detail.getBattery())
                .camera(detail.getCamera())
                .os(detail.getOs())
                .chargingSpeed(detail.getChargingSpeed())
                .connectivity(detail.getConnectivity())
                .build();
    }
}