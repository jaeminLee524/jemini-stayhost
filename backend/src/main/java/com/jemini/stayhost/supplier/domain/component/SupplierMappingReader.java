package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.model.MappingStatus;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;

import java.util.List;
import java.util.Optional;

public interface SupplierMappingReader {

    Optional<SupplierPropertyMapping> findBySupplierPropertyId(Long supplierPropertyId);

    List<SupplierPropertyMapping> findBySupplierPropertyIds(List<Long> supplierPropertyIds, MappingStatus status);
}
