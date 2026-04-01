package com.jemini.stayhost.property.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyStatus {

    ACTIVE("노출 중"),
    INACTIVE("비노출"),
    ;

    private final String description;
}
