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

}
