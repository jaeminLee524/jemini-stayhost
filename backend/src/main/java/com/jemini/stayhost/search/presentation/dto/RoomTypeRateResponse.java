package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.DailyRateResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeRateResponse(
    Long propertyId,
    List<RoomTypeRateEntryResponse> roomTypes
) {

    public static RoomTypeRateResponse from(final RoomTypeRateResult result) {
        return RoomTypeRateResponse.builder()
            .propertyId(result.propertyId())
            .roomTypes(mapToRoomTypes(result))
            .build();
    }

    private static List<RoomTypeRateEntryResponse> mapToRoomTypes(RoomTypeRateResult result) {
        return result.roomTypes().stream()
            .map(RoomTypeRateResponse::mapToRoomTypeRateEntry)
            .toList();
    }

    private static RoomTypeRateEntryResponse mapToRoomTypeRateEntry(RoomTypeRateEntryResult rt) {
        return RoomTypeRateEntryResponse.builder()
            .id(rt.id())
            .name(rt.name())
            .maxOccupancy(rt.maxOccupancy())
            .rates(mapToDailyRates(rt.rates()))
            .build();
    }

    private static List<DailyRateResponse> mapToDailyRates(List<DailyRateResult> rates) {
        return rates.stream()
            .map(RoomTypeRateResponse::mapToDailyRate)
            .toList();
    }

    private static DailyRateResponse mapToDailyRate(DailyRateResult r) {
        return DailyRateResponse.builder()
            .date(r.date())
            .price(r.price())
            .available(r.available())
            .build();
    }
}
