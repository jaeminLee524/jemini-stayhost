package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.supplier.domain.component.SupplierMappingReader;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierPropertyMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplierMappingReaderImpl implements SupplierMappingReader {

    private final SupplierPropertyMappingRepository supplierPropertyMappingRepository;

    @Override
    public Optional<SupplierPropertyMapping> findBySupplierPropertyId(final Long supplierPropertyId) {
        return supplierPropertyMappingRepository.findBySupplierPropertyId(supplierPropertyId);
    }
}
