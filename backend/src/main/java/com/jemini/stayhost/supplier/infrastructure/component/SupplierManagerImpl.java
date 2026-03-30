package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.supplier.domain.component.SupplierManager;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierPropertyMappingRepository;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierPropertyRepository;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierSyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SupplierManagerImpl implements SupplierManager {

    private final SupplierPropertyRepository supplierPropertyRepository;
    private final SupplierPropertyMappingRepository supplierPropertyMappingRepository;
    private final SupplierSyncJobRepository supplierSyncJobRepository;

    @Override
    public SupplierProperty saveProperty(final SupplierProperty supplierProperty) {
        return supplierPropertyRepository.save(supplierProperty);
    }

    @Override
    public SupplierPropertyMapping saveMapping(final SupplierPropertyMapping mapping) {
        return supplierPropertyMappingRepository.save(mapping);
    }

    /**
     * 동기화 작업 이력을 독립 트랜잭션으로 저장한다.
     * 외부 트랜잭션이 롤백되더라도 FAILED/COMPLETED 상태가 유실되지 않도록 보장한다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SupplierSyncJob saveSyncJob(final SupplierSyncJob syncJob) {
        return supplierSyncJobRepository.save(syncJob);
    }
}
