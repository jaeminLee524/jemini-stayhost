package com.jemini.stayhost.user.application.dto;

import com.jemini.stayhost.user.domain.model.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResult(
    Long id,
    String email,
    String name,
    String phone,
    String status,
    LocalDateTime createdAt
) {

    public static UserResult from(final User user) {
        return UserResult.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .status(user.getStatus().name())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
