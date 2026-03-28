package com.jemini.stayhost.common.security;

public record UserId(Long value) {

    public static UserId of(final Long value) {
        return new UserId(value);
    }
}
