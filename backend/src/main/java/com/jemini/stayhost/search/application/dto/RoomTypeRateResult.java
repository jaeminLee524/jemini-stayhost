package com.jemini.stayhost.search.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RoomTypeRateResult(
    Long propertyId,
    List<RoomTypeRateEntryResult> roomTypes
) {

}
