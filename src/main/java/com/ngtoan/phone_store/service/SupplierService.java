package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.SupplierRequest;
import com.ngtoan.phone_store.dto.request.SupplierUpdateRequest;
import com.ngtoan.phone_store.dto.response.SupplierResponse;
import com.ngtoan.phone_store.entity.Supplier;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.DuplicateResourceException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";

    private final SupplierRepository supplierRepository;

    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SupplierResponse> getActive() {
        return supplierRepository.findByStatus(ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse getById(Integer id) {
        Supplier supplier = getSupplierEntity(id);
        return toResponse(supplier);
    }

    public SupplierResponse create(SupplierRequest request) {
        String name = normalizeRequiredName(request.getName());

        if (supplierRepository.existsByNameIgnoreCaseCustom(name)) {
            throw new DuplicateResourceException("Supplier name already exists");
        }

        Supplier supplier = Supplier.builder()
                .name(name)
                .phone(normalize(request.getPhone()))
                .email(normalize(request.getEmail()))
                .address(normalize(request.getAddress()))
                .status(ACTIVE)
                .build();

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Integer id, SupplierUpdateRequest request) {
        Supplier supplier = getSupplierEntity(id);

        if (request.getName() != null) {
            String name = normalizeRequiredName(request.getName());
            if (supplierRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new DuplicateResourceException("Supplier name already exists");
            }
            supplier.setName(name);
        }

        if (request.getPhone() != null) {
            supplier.setPhone(normalize(request.getPhone()));
        }

        if (request.getEmail() != null) {
            supplier.setEmail(normalize(request.getEmail()));
        }

        if (request.getAddress() != null) {
            supplier.setAddress(normalize(request.getAddress()));
        }

        if (request.getStatus() != null) {
            supplier.setStatus(normalizeStatus(request.getStatus()));
        }

        return toResponse(supplierRepository.save(supplier));
    }

    public void hide(Integer id) {
        Supplier supplier = getSupplierEntity(id);
        supplier.setStatus(INACTIVE);
        supplierRepository.save(supplier);
    }

    private Supplier getSupplierEntity(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private String normalizeRequiredName(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            throw new BadRequestException("Supplier name is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (normalized == null) {
            throw new BadRequestException("Status is required");
        }

        normalized = normalized.toUpperCase();
        if (!normalized.equals(ACTIVE) && !normalized.equals(INACTIVE)) {
            throw new BadRequestException("Supplier status must be ACTIVE or INACTIVE");
        }

        return normalized;
    }

    private SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setSupplierID(supplier.getSupplierID());
        response.setName(supplier.getName());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setStatus(supplier.getStatus());
        response.setCreatedDate(supplier.getCreatedDate());
        response.setUpdatedDate(supplier.getUpdatedDate());
        return response;
    }
}
