package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserLoginResult;
import lombok.Builder;

@Builder
public record UserLoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
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
