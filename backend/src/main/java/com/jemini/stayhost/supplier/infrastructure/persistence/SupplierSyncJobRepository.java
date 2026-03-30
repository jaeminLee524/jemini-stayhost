package com.jemini.stayhost.supplier.infrastructure.persistence;

import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierSyncJobRepository extends JpaRepository<SupplierSyncJob, Long> {
}
