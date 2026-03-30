package com.jemini.stayhost.channel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarkupType {

    PERCENTAGE("비율"),
    FIXED("고정금액"),
    ;

    private final String description;
}
