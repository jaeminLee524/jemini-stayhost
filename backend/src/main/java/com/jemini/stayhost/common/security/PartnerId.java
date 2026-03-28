package com.jemini.stayhost.common.security;

public record PartnerId(Long value) {

    public static PartnerId of(final Long value) {
        return new PartnerId(value);
    }
}
