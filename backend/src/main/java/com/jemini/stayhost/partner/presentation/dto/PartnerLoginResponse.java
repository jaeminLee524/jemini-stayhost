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
    PartnerInfoResponse partner
) {

    public static PartnerLoginResponse from(final PartnerLoginResult result) {
        return PartnerLoginResponse.builder()
            .accessToken(result.accessToken())
            .tokenType("Bearer")
            .expiresIn(result.expiresInSeconds())
            .partner(PartnerInfoResponse.from(result))
            .build();
    }
}
