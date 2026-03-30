package com.jemini.stayhost.channel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelSyncStatus {

    SUCCESS("성공"),
    FAILED("실패"),
    PARTIAL("부분 성공"),
    ;

    private final String description;
}
