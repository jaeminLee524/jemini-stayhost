package com.jemini.stayhost.partner.application.dto;

public record PartnerLoginCommand(
    String loginId,
    String password
) {

  public static PartnerLoginCommand create(final String loginId, final String password) {
    return new PartnerLoginCommand(loginId, password);
  }
}
