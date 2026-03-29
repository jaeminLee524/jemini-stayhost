package com.jemini.stayhost.common.response;

import com.jemini.stayhost.common.exception.ErrorCode;

public record ApiBaseResponse<T>(
    ResultType result,
    T data,
    ErrorMessage error
) {

    public static <T> ApiBaseResponse<T> success(final T data) {
        return new ApiBaseResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiBaseResponse<Void> success() {
        return new ApiBaseResponse<>(ResultType.SUCCESS, null, null);
    }

    public static ApiBaseResponse<?> error(final ErrorCode errorCode, final String message) {
        return new ApiBaseResponse<>(ResultType.ERROR, null, new ErrorMessage(errorCode, message));
    }
}
