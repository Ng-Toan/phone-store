package com.ngtoan.phone_store.controller;

import com.ngtoan.phone_store.dto.request.ImportReceiptRequest;
import com.ngtoan.phone_store.dto.response.ImportReceiptResponse;
import com.ngtoan.phone_store.service.ImportReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ImportReceiptResponse> getAll() {
        return importReceiptService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ImportReceiptResponse getById(@PathVariable Integer id) {
        return importReceiptService.getById(id);
    }

    @GetMapping("/supplier/{supplierID}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ImportReceiptResponse> getBySupplier(@PathVariable Integer supplierID) {
        return importReceiptService.getBySupplier(supplierID);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ImportReceiptResponse create(@Valid @RequestBody ImportReceiptRequest request) {
        return importReceiptService.create(request);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ImportReceiptResponse cancel(@PathVariable Integer id) {
        return importReceiptService.cancel(id);
    }
}
