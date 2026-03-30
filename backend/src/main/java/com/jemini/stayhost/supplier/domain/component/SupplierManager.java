package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;

public interface SupplierManager {

    SupplierProperty saveProperty(SupplierProperty supplierProperty);

    SupplierPropertyMapping saveMapping(SupplierPropertyMapping mapping);

    SupplierSyncJob saveSyncJob(SupplierSyncJob syncJob);
}
