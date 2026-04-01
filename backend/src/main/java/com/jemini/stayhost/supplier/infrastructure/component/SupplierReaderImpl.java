package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.supplier.domain.component.SupplierReader;
import com.jemini.stayhost.supplier.domain.model.Supplier;
import com.jemini.stayhost.supplier.domain.model.SupplierStatus;
import com.jemini.stayhost.supplier.infrastructure.persistence.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SupplierReaderImpl implements SupplierReader {

    private final SupplierRepository supplierRepository;

    @Override
    public Supplier getById(final Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Override
    public List<Supplier> findActiveSuppliers() {
        return supplierRepository.findByStatus(SupplierStatus.ACTIVE);
    }
}
