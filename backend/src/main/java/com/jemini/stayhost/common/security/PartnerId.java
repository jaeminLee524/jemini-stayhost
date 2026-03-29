package com.jemini.stayhost.common.security;

public record PartnerId(
  Long value
) {

  public static PartnerId create(final Long value) {
    return new PartnerId(value);
  }
}
