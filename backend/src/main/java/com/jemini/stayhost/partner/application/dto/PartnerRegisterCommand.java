package com.jemini.stayhost.partner.application.dto;

public record PartnerRegisterCommand(
    String businessName,
    String businessNumber,
    String representative,
    String phone,
    String email,
    String bankName,
    String bankAccount,
    String loginId,
    String password
) {

  public static PartnerRegisterCommand create(
      final String businessName,
      final String businessNumber,
      final String representative,
      final String phone,
      final String email,
      final String bankName,
      final String bankAccount,
      final String loginId,
      final String password
  ) {
    return new PartnerRegisterCommand(
        businessName, businessNumber, representative,
        phone, email, bankName, bankAccount, loginId, password
    );
  }
}
