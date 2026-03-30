package com.jemini.stayhost.supplier.domain.dto;

public record SupplierPropertyData(
        String externalPropertyId,
        String name,
        String address,
        String region,
        String type,
        String rawData
) {
}
