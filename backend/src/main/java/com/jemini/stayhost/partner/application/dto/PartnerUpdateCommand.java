package com.jemini.stayhost.partner.application.dto;

public record PartnerUpdateCommand(
    String phone,
    String email,
    String bankName,
    String bankAccount
) {

  public static PartnerUpdateCommand create(
      final String phone,
      final String email,
      final String bankName,
      final String bankAccount
  ) {
    return new PartnerUpdateCommand(phone, email, bankName, bankAccount);
  }
}
