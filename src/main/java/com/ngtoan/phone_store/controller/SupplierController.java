package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.SupplierRequest;
import com.ngtoan.phone_store.dto.request.SupplierUpdateRequest;
import com.ngtoan.phone_store.dto.response.SupplierResponse;
import com.ngtoan.phone_store.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SupplierResponse> getAll() {
        return supplierService.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SupplierResponse> getActive() {
        return supplierService.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse getById(@PathVariable Integer id) {
        return supplierService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse update(@PathVariable Integer id,
                                   @Valid @RequestBody SupplierUpdateRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void hide(@PathVariable Integer id) {
        supplierService.hide(id);
    }
}
