package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerLoginResult;
import lombok.Builder;

@Builder
public record PartnerLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        PartnerInfo partner
) {

    @Builder
    public record PartnerInfo(
            Long id,
            String businessName,
            String status
    ) {

        public static PartnerInfo from(final PartnerLoginResult result) {
            return PartnerInfo.builder()
                    .id(result.partnerId())
                    .businessName(result.businessName())
                    .status(result.status())
                    .build();
        }
    }

    public static PartnerLoginResponse from(final PartnerLoginResult result) {
        return PartnerLoginResponse.builder()
                .accessToken(result.accessToken())
                .tokenType("Bearer")
                .expiresIn(result.expiresInSeconds())
                .partner(PartnerInfo.from(result))
                .build();
    }
}
