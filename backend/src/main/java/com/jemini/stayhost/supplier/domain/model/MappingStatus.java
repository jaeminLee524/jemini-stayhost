package com.jemini.stayhost.supplier.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MappingStatus {

    MAPPED("매핑완료"),
    UNMAPPED("미매핑"),
    CONFLICT("충돌"),
    ;

    private final String description;
}
