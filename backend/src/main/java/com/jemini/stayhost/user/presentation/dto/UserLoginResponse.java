package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserLoginResult;
import lombok.Builder;

@Builder
public record UserLoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {

    @Builder
    public record UserInfo(
        Long id,
        String email,
        String name
    ) {}

    public static UserLoginResponse from(final UserLoginResult result) {
        return UserLoginResponse.builder()
            .accessToken(result.accessToken())
            .tokenType("Bearer")
            .expiresIn(result.expiresIn())
            .user(UserInfo.builder()
                .id(result.userId())
                .email(result.email())
                .name(result.name())
                .build())
            .build();
    }
}
