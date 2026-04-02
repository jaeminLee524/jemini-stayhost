package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "로그인 응답")
public record UserLoginResponse(
    @Schema(description = "액세스 토큰")
    String accessToken,

    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,

    @Schema(description = "토큰 만료 시간 (초)", example = "86400")
    long expiresIn,

    @Schema(description = "로그인한 회원 정보")
    UserInfoResponse user
) {

    public static UserLoginResponse from(final UserLoginResult result) {
        return UserLoginResponse.builder()
            .accessToken(result.accessToken())
            .tokenType("Bearer")
            .expiresIn(result.expiresIn())
            .user(mapToUserInfo(result))
            .build();
    }

    private static UserInfoResponse mapToUserInfo(UserLoginResult result) {
        return UserInfoResponse.builder()
            .id(result.userId())
            .email(result.email())
            .name(result.name())
            .build();
    }
}
