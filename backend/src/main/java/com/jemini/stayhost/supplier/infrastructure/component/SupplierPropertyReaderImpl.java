package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.supplier.domain.component.SupplierPropertyReader;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplierPropertyReaderImpl implements SupplierPropertyReader {

    private final SupplierPropertyRepository supplierPropertyRepository;

    @Override
    public Optional<SupplierProperty> findBySupplierIdAndExternalPropertyId(final Long supplierId, final String externalPropertyId) {
        return supplierPropertyRepository.findBySupplierIdAndExternalPropertyId(supplierId, externalPropertyId);
    }

    @Override
    public List<SupplierProperty> findBySupplierId(final Long supplierId) {
        return supplierPropertyRepository.findBySupplierId(supplierId);
    }
}
