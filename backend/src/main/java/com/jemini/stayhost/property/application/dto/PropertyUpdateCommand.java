package com.jemini.stayhost.property.application.dto;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record PropertyUpdateCommand(
    String name,
    String description,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String thumbnailUrl
) {

  public static PropertyUpdateCommand create(
      final String name,
      final String description,
      final LocalTime checkInTime,
      final LocalTime checkOutTime,
      final String thumbnailUrl
  ) {
    return PropertyUpdateCommand.builder()
        .name(name)
        .description(description)
        .checkInTime(checkInTime)
        .checkOutTime(checkOutTime)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }
}
