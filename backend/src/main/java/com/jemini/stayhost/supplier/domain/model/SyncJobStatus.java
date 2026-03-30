package com.jemini.stayhost.supplier.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncJobStatus {

    RUNNING("진행중"),
    COMPLETED("완료"),
    FAILED("실패"),
    ;

    private final String description;
}
