package com.jemini.stayhost.property.application.dto;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record PropertyCreateCommand(
    String name,
    String type,
    String description,
    String address,
    String region,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String thumbnailUrl
) {

  public static PropertyCreateCommand create(
      final String name,
      final String type,
      final String description,
      final String address,
      final String region,
      final LocalTime checkInTime,
      final LocalTime checkOutTime,
      final String thumbnailUrl
  ) {
    return PropertyCreateCommand.builder()
        .name(name)
        .type(type)
        .description(description)
        .address(address)
        .region(region)
        .checkInTime(checkInTime)
        .checkOutTime(checkOutTime)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }
}
