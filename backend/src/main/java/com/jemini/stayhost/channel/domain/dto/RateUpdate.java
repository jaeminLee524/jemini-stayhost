package com.jemini.stayhost.channel.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateUpdate(
    String externalRoomId,
    LocalDate date,
    BigDecimal price
) {
}
