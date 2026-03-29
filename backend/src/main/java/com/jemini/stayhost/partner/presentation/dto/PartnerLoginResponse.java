package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "파트너 로그인 응답")
public record PartnerLoginResponse(
        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "만료 시간(초)", example = "1800")
        long expiresIn,

        @Schema(description = "파트너 정보")
        PartnerInfo partner
) {

    @Builder
    @Schema(description = "로그인한 파트너 정보")
    public record PartnerInfo(
            @Schema(description = "파트너 ID", example = "10")
            Long id,

            @Schema(description = "사업자명", example = "㈜스테이호스트")
            String businessName,

            @Schema(description = "상태", example = "ACTIVE")
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
