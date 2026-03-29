package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserResult;
import lombok.Builder;

@Builder
public record UserSignupResponse(
    Long id,
    String email,
    String name
) {

    public static UserSignupResponse from(final UserResult result) {
        return UserSignupResponse.builder()
            .id(result.id())
            .email(result.email())
            .name(result.name())
            .build();
    }
}
