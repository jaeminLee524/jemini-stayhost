package com.jemini.stayhost.property.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomTypeStatus {

    ACTIVE("판매 중"),
    INACTIVE("판매 중지"),
    ;

    private final String description;
}
