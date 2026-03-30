package com.jemini.stayhost.supplier.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_property")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierProperty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false, length = 100)
    private String externalPropertyId;

    @Column(nullable = false, columnDefinition = "json")
    private String rawData;

    private LocalDateTime lastSyncedAt;

    public static SupplierProperty create(
        final Long supplierId,
        final String externalPropertyId,
        final String rawData
    ) {
        final SupplierProperty supplierProperty = new SupplierProperty();
        supplierProperty.supplierId = supplierId;
        supplierProperty.externalPropertyId = externalPropertyId;
        supplierProperty.rawData = rawData;
        return supplierProperty;
    }

    public void updateRawData(final String rawData) {
        this.rawData = rawData;
        this.lastSyncedAt = LocalDateTime.now();
    }
}
