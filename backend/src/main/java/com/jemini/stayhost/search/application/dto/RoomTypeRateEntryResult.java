package com.jemini.stayhost.search.application.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeRateEntryResult(
    Long id,
    String name,
    int maxOccupancy,
    List<DailyRateResult> rates
) {

}
