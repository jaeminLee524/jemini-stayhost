package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.model.SupplierProperty;

import java.util.List;
import java.util.Optional;

public interface SupplierPropertyReader {

    Optional<SupplierProperty> findBySupplierIdAndExternalPropertyId(Long supplierId, String externalPropertyId);

    List<SupplierProperty> findBySupplierId(Long supplierId);
}
