package com.jemini.stayhost.supplier.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncType {

    PULL("배치"),
    PUSH("웹훅"),
    ;

    private final String description;
}
