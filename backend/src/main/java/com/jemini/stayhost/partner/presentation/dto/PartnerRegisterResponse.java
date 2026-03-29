package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerResult;
import lombok.Builder;

@Builder
public record PartnerRegisterResponse(
        Long partnerId,
        String businessName,
        String status
) {

    public static PartnerRegisterResponse from(final PartnerResult result) {
        return PartnerRegisterResponse.builder()
                .partnerId(result.id())
                .businessName(result.businessName())
                .status(result.status())
                .build();
    }
}
