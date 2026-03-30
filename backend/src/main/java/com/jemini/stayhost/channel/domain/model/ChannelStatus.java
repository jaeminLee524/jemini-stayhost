package com.jemini.stayhost.channel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    ;

    private final String description;
}
