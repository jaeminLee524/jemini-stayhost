package com.jemini.stayhost.search.presentation.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeRateEntryResponse(
    Long id,
    String name,
    int maxOccupancy,
    List<DailyRateResponse> rates
) {

}
