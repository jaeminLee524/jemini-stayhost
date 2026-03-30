package com.jemini.stayhost.supplier.infrastructure.persistence;

import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierPropertyMappingRepository extends JpaRepository<SupplierPropertyMapping, Long> {

    Optional<SupplierPropertyMapping> findBySupplierPropertyId(Long supplierPropertyId);
}
