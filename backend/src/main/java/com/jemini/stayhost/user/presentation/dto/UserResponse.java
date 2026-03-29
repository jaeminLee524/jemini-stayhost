package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserResult;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
    Long id,
    String email,
    String name,
    String phone,
    String status,
    LocalDateTime createdAt
) {

    public static UserResponse from(final UserResult result) {
        return UserResponse.builder()
            .id(result.id())
            .email(result.email())
            .name(result.name())
            .phone(result.phone())
            .status(result.status())
            .createdAt(result.createdAt())
            .build();
    }
}
