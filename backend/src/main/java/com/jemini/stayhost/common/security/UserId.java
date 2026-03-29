package com.jemini.stayhost.common.security;

public record UserId(
  Long value
) {

  public static UserId create(final Long value) {
    return new UserId(value);
  }
}
