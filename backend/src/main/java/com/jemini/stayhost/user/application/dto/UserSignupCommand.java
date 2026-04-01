package com.jemini.stayhost.user.application.dto;

import lombok.Builder;

@Builder
public record UserSignupCommand(
    String email,
    String password,
    String name,
    String phone
) {

}
