package com.jemini.stayhost.channel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncDirection {

    OUTBOUND("자사→채널"),
    INBOUND("채널→자사"),
    ;

    private final String description;
}
