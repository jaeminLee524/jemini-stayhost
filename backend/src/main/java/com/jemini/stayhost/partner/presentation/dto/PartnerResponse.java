package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerResult;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartnerResponse(
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

    public static PartnerResponse from(final PartnerResult result) {
        return PartnerResponse.builder()
                .id(result.id())
                .businessName(result.businessName())
                .businessNumber(result.businessNumber())
                .representative(result.representative())
                .phone(result.phone())
                .email(result.email())
                .bankName(result.bankName())
                .bankAccount(result.bankAccount())
                .status(result.status())
                .createdAt(result.createdAt())
                .build();
    }
}
