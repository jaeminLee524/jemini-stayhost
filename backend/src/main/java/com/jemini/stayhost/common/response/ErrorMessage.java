package com.jemini.stayhost.common.response;

import com.jemini.stayhost.common.exception.ErrorCode;

public record ErrorMessage(
        String code,
        String message
) {

    public ErrorMessage(final ErrorCode errorCode, final String message) {
        this(errorCode.name(), message);
    }
}
