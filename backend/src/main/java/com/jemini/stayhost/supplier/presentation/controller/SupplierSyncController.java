package com.jemini.stayhost.supplier.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.supplier.application.service.SupplierSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierSyncController {

    private final SupplierSyncService supplierSyncService;

    @PostMapping("/{supplierId}/sync")
    public ApiBaseResponse<Void> syncSupplier(
        @PathVariable final Long supplierId
    ) {
        supplierSyncService.syncSupplier(supplierId);
        return ApiBaseResponse.success(null);
    }
}
