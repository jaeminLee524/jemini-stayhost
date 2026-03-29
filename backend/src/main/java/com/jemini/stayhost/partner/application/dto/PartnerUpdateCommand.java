package com.jemini.stayhost.partner.application.dto;

import lombok.Builder;

@Builder
public record PartnerUpdateCommand(
    String phone,
    String email,
    String bankName,
    String bankAccount
) {
}
