package com.jemini.stayhost.user.application.dto;

import lombok.Builder;

@Builder
public record UserLoginResult(
    String accessToken,
    long expiresIn,
    Long userId,
    String email,
    String name
) {

}
