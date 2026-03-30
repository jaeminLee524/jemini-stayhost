package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.model.Supplier;

import java.util.List;

public interface SupplierReader {

    Supplier getById(Long id);

    List<Supplier> findActiveSuppliers();
}
