package com.jemini.stayhost.channel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelSyncType {

    INVENTORY("재고"),
    RATE("요금"),
    RESERVATION("예약"),
    ;

    private final String description;
}
