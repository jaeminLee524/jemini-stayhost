package com.jemini.stayhost.partner.application.dto;

import lombok.Builder;

@Builder
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
}
