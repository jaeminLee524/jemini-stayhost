package com.jemini.stayhost.user.application.dto;

import lombok.Builder;

@Builder
public record UserLoginCommand(
    String email,
    String password
) {

}
