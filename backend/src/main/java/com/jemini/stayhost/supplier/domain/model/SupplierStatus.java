package com.jemini.stayhost.supplier.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    ;

    private final String description;
}
