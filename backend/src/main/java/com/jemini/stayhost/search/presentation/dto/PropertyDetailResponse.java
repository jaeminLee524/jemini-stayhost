package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertyImageEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeEntryResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.jspecify.annotations.NonNull;

@Builder
public record PropertyDetailResponse(
    Long id,
    String name,
    String type,
    String description,
    String address,
    String region,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String thumbnailUrl,
    List<ImageEntryResponse> images,
    List<RoomTypeEntryResponse> roomTypes
) {

    public static PropertyDetailResponse from(final PropertyDetailResult result) {
        return PropertyDetailResponse.builder()
            .id(result.id())
            .name(result.name())
            .type(result.type())
            .description(result.description())
            .address(result.address())
            .region(result.region())
            .latitude(result.latitude())
            .longitude(result.longitude())
            .checkInTime(result.checkInTime())
            .checkOutTime(result.checkOutTime())
            .thumbnailUrl(result.thumbnailUrl())
            .images(mapToImageEntries(result))
            .roomTypes(mapToRooTypes(result))
            .build();
    }

    private static List<ImageEntryResponse> mapToImageEntries(PropertyDetailResult result) {
        return result.images().stream()
            .map(PropertyDetailResponse::mapToImageEntry)
            .toList();
    }

    private static ImageEntryResponse mapToImageEntry(PropertyImageEntryResult img) {
        return ImageEntryResponse.builder()
            .imageUrl(img.imageUrl())
            .sortOrder(img.sortOrder())
            .build();
    }

    private static List<RoomTypeEntryResponse> mapToRooTypes(PropertyDetailResult result) {
        return result.roomTypes().stream()
            .map(PropertyDetailResponse::mapToRoomTypeEntry)
            .toList();
    }

    private static RoomTypeEntryResponse mapToRoomTypeEntry(RoomTypeEntryResult rt) {
        return RoomTypeEntryResponse.builder()
            .id(rt.id())
            .name(rt.name())
            .description(rt.description())
            .maxOccupancy(rt.maxOccupancy())
            .basePrice(rt.basePrice())
            .amenities(rt.amenities())
            .build();
    }
}
