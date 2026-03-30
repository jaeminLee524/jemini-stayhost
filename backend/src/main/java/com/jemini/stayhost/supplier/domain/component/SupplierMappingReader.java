package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;

import java.util.Optional;

public interface SupplierMappingReader {

    Optional<SupplierPropertyMapping> findBySupplierPropertyId(Long supplierPropertyId);
}
