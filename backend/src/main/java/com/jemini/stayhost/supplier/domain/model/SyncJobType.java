package com.jemini.stayhost.supplier.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncJobType {

    FULL_SYNC("전체 동기화"),
    INCREMENTAL("증분 동기화"),
    RATE_UPDATE("요금 업데이트"),
    INVENTORY_UPDATE("재고 업데이트"),
    ;

    private final String description;
}
