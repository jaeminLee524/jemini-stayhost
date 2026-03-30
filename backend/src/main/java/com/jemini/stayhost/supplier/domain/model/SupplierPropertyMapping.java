package com.jemini.stayhost.supplier.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier_property_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierPropertyMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierPropertyId;

    @Column(nullable = false)
    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MappingStatus mappingStatus;

    public static SupplierPropertyMapping create(
        final Long supplierPropertyId,
        final Long propertyId
    ) {
        final SupplierPropertyMapping mapping = new SupplierPropertyMapping();
        mapping.supplierPropertyId = supplierPropertyId;
        mapping.propertyId = propertyId;
        mapping.mappingStatus = MappingStatus.UNMAPPED;
        return mapping;
    }

    public void map() {
        this.mappingStatus = MappingStatus.MAPPED;
    }

    public void markConflict() {
        this.mappingStatus = MappingStatus.CONFLICT;
    }
}
