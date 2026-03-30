package com.jemini.stayhost.supplier.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.supplier.domain.component.SupplierManager;
import com.jemini.stayhost.supplier.domain.component.SupplierPropertyReader;
import com.jemini.stayhost.supplier.domain.component.SupplierReader;
import com.jemini.stayhost.supplier.domain.model.Supplier;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import com.jemini.stayhost.supplier.domain.model.SyncJobType;
import com.jemini.stayhost.supplier.domain.component.SupplierAdapter;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierSyncService {

    private final SupplierReader supplierReader;
    private final SupplierPropertyReader supplierPropertyReader;
    private final SupplierManager supplierManager;
    private final List<SupplierAdapter> adapters;

    @Transactional
    public void syncSupplier(final Long supplierId) {
        final Supplier supplier = supplierReader.getById(supplierId);
        final SupplierAdapter adapter = findAdapter(supplier.getCode());
        final SupplierSyncJob syncJob = supplierManager.saveSyncJob(SupplierSyncJob.start(supplierId, SyncJobType.FULL_SYNC));

        try {
            final List<SupplierPropertyData> properties = adapter.fetchProperties();
            int successCount = 0;
            int failCount = 0;

            for (final SupplierPropertyData data : properties) {
                try {
                    syncProperty(supplier.getId(), data);
                    successCount++;
                } catch (final Exception e) {
                    log.warn("공급사 숙소 동기화 실패: supplierId={}, externalId={}", supplierId, data.externalPropertyId(), e);
                    failCount++;
                }
            }

            syncJob.complete(properties.size(), successCount, failCount);
            supplierManager.saveSyncJob(syncJob);
        } catch (final Exception e) {
            log.error("공급사 동기화 실패: supplierId={}", supplierId, e);
            syncJob.fail(e.getMessage());
            supplierManager.saveSyncJob(syncJob);
        }
    }

    private SupplierAdapter findAdapter(final String supplierCode) {
        return adapters.stream()
                .filter(adapter -> adapter.getSupplierCode().equals(supplierCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_ADAPTER_NOT_FOUND));
    }

    private void syncProperty(final Long supplierId, final SupplierPropertyData data) {
        supplierPropertyReader.findBySupplierIdAndExternalPropertyId(supplierId, data.externalPropertyId())
            .ifPresentOrElse(
                property -> property.updateRawData(data.rawData()),
                () -> supplierManager.saveProperty(SupplierProperty.create(supplierId, data.externalPropertyId(), data.rawData()))
            );
    }
}
