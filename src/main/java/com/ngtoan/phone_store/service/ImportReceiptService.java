package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ImportDetailRequest;
import com.ngtoan.phone_store.dto.request.ImportReceiptRequest;
import com.ngtoan.phone_store.dto.response.ImportDetailResponse;
import com.ngtoan.phone_store.dto.response.ImportReceiptResponse;
import com.ngtoan.phone_store.entity.ImportDetail;
import com.ngtoan.phone_store.entity.ImportReceipt;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.entity.Supplier;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.ImportDetailRepository;
import com.ngtoan.phone_store.repository.ImportReceiptRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import com.ngtoan.phone_store.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportReceiptService {

    private static final String ACTIVE = "ACTIVE";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELLED = "CANCELLED";

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportDetailRepository importDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public List<ImportReceiptResponse> getAll() {
        return importReceiptRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ImportReceiptResponse getById(Integer id) {
        ImportReceipt receipt = getImportReceiptEntity(id);
        return toResponse(receipt);
    }

    public List<ImportReceiptResponse> getBySupplier(Integer supplierID) {
        supplierRepository.findById(supplierID)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierID));

        return importReceiptRepository.findBySupplierIDOrderByCreatedDateDesc(supplierID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ImportReceiptResponse create(ImportReceiptRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierID())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierID()));

        if (!ACTIVE.equalsIgnoreCase(supplier.getStatus())) {
            throw new BadRequestException("Supplier is inactive");
        }

        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new BadRequestException("Import details must not be empty");
        }

        List<PreparedImportItem> preparedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ImportDetailRequest item : request.getDetails()) {
            validateImportDetail(item);

            Product product = productRepository.findByIdForUpdate(item.getProductID());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + item.getProductID());
            }

            BigDecimal subTotal = item.getImportPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            totalAmount = totalAmount.add(subTotal);
            preparedItems.add(new PreparedImportItem(product, item));
        }

        ImportReceipt receipt = ImportReceipt.builder()
                .supplierID(supplier.getSupplierID())
                .totalAmount(totalAmount)
                .note(normalize(request.getNote()))
                .status(COMPLETED)
                .build();

        importReceiptRepository.save(receipt);

        for (PreparedImportItem preparedItem : preparedItems) {
            Product product = preparedItem.product();
            ImportDetailRequest item = preparedItem.item();

            ImportDetail detail = ImportDetail.builder()
                    .importID(receipt.getImportID())
                    .productID(product.getProductID())
                    .quantity(item.getQuantity())
                    .importPrice(item.getImportPrice())
                    .build();

            importDetailRepository.save(detail);

            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        return toResponse(receipt);
    }

    public ImportReceiptResponse cancel(Integer id) {
        ImportReceipt receipt = getImportReceiptEntity(id);

        if (CANCELLED.equalsIgnoreCase(receipt.getStatus())) {
            throw new BadRequestException("Import receipt has already been cancelled");
        }

        List<ImportDetail> details = importDetailRepository.findByImportID(receipt.getImportID());

        for (ImportDetail detail : details) {
            Product product = productRepository.findByIdForUpdate(detail.getProductID());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + detail.getProductID());
            }

            if (product.getQuantity() < detail.getQuantity()) {
                throw new BadRequestException(
                        "Cannot cancel import receipt because stock is not enough for product: " + product.getName()
                );
            }

            product.setQuantity(product.getQuantity() - detail.getQuantity());
            productRepository.save(product);
        }

        receipt.setStatus(CANCELLED);
        importReceiptRepository.save(receipt);

        return toResponse(receipt);
    }

    private void validateImportDetail(ImportDetailRequest item) {
        if (item == null) {
            throw new BadRequestException("Import detail is required");
        }

        if (item.getProductID() == null) {
            throw new BadRequestException("Product is required");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        if (item.getImportPrice() == null || item.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Import price must be greater than 0");
        }
    }

    private ImportReceipt getImportReceiptEntity(Integer id) {
        return importReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import receipt not found with id: " + id));
    }

private ImportReceiptResponse toResponse(ImportReceipt receipt) {
    ImportReceiptResponse response = new ImportReceiptResponse();

    response.setImportID(receipt.getImportID());
    response.setSupplierID(receipt.getSupplierID());

    Supplier supplier = supplierRepository.findById(receipt.getSupplierID())
            .orElse(null);

    response.setSupplierName(
            supplier != null ? supplier.getName() : null
    );

    response.setCreatedDate(receipt.getCreatedDate());
    response.setTotalAmount(receipt.getTotalAmount());
    response.setNote(receipt.getNote());
    response.setStatus(receipt.getStatus());

    List<ImportDetailResponse> details =
            importDetailRepository.findByImportID(receipt.getImportID())
                    .stream()
                    .map(this::toDetailResponse)
                    .toList();

    response.setDetails(details);

    return response;
}

private ImportDetailResponse toDetailResponse(ImportDetail detail) {
    ImportDetailResponse response = new ImportDetailResponse();

    response.setImportDetailID(detail.getImportDetailID());
    response.setProductID(detail.getProductID());

    Product product = productRepository.findById(detail.getProductID())
            .orElse(null);

    response.setProductName(
            product != null ? product.getName() : null
    );

    response.setImage(
            product != null ? product.getImage() : null
    );

    response.setQuantity(detail.getQuantity());
    response.setImportPrice(detail.getImportPrice());

    BigDecimal subTotal = detail.getSubTotal();

    if (subTotal == null
            && detail.getImportPrice() != null
            && detail.getQuantity() != null) {

        subTotal = detail.getImportPrice()
                .multiply(BigDecimal.valueOf(detail.getQuantity()));
    }

    response.setSubTotal(subTotal);

    return response;
}

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record PreparedImportItem(Product product, ImportDetailRequest item) {}
}
