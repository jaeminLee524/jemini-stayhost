package com.jemini.stayhost.user.presentation.dto;

import lombok.Builder;

@Builder
public record UserInfoResponse(
    Long id,
    String email,
    String name
) {

}
