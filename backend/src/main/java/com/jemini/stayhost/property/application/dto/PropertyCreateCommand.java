package com.jemini.stayhost.property.application.dto;

import java.math.BigDecimal;
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
    BigDecimal latitude,
    BigDecimal longitude,
    String thumbnailUrl
) {

}
