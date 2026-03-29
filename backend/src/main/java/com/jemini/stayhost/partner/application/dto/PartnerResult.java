package com.jemini.stayhost.partner.application.dto;

import com.jemini.stayhost.partner.domain.model.Partner;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartnerResult(
        Long id,
        String businessName,
        String businessNumber,
        String representative,
        String phone,
        String email,
        String bankName,
        String bankAccount,
        String status,
        LocalDateTime createdAt
) {

    public static PartnerResult from(final Partner partner) {
        return PartnerResult.builder()
                .id(partner.getId())
                .businessName(partner.getBusinessName())
                .businessNumber(partner.getBusinessNumber())
                .representative(partner.getRepresentative())
                .phone(partner.getPhone())
                .email(partner.getEmail())
                .bankName(partner.getBankName())
                .bankAccount(partner.getBankAccount())
                .status(partner.getStatus().name())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}
