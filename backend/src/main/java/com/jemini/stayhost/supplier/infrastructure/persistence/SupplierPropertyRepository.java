package com.jemini.stayhost.supplier.infrastructure.persistence;

import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierPropertyRepository extends JpaRepository<SupplierProperty, Long> {

    Optional<SupplierProperty> findBySupplierIdAndExternalPropertyId(Long supplierId, String externalPropertyId);

    List<SupplierProperty> findBySupplierId(Long supplierId);
}
