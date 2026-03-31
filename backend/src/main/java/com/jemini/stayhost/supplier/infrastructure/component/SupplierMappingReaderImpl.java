package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.supplier.domain.component.SupplierMappingReader;
import com.jemini.stayhost.supplier.domain.model.MappingStatus;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierPropertyMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplierMappingReaderImpl implements SupplierMappingReader {

    private final SupplierPropertyMappingRepository supplierPropertyMappingRepository;

    @Override
    public Optional<SupplierPropertyMapping> findBySupplierPropertyId(final Long supplierPropertyId) {
        return supplierPropertyMappingRepository.findBySupplierPropertyId(supplierPropertyId);
    }

    @Override
    public List<SupplierPropertyMapping> findBySupplierPropertyIds(final List<Long> supplierPropertyIds, final MappingStatus status) {
        if (supplierPropertyIds.isEmpty()) {
            return List.of();
        }
        return supplierPropertyMappingRepository.findBySupplierPropertyIdInAndMappingStatus(supplierPropertyIds, status);
    }
}
