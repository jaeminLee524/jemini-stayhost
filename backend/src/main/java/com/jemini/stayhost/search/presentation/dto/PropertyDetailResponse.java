package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertyImageEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeImageEntryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Builder
@Schema(description = "숙소 상세 응답")
public record PropertyDetailResponse(
    @Schema(description = "숙소 ID", example = "100")
    Long id,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String name,

    @Schema(description = "숙소 유형", example = "HOTEL")
    String type,

    @Schema(description = "숙소 설명")
    String description,

    @Schema(description = "주소")
    String address,

    @Schema(description = "지역", example = "서울")
    String region,

    @Schema(description = "위도", example = "37.4979000")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.0276000")
    BigDecimal longitude,

    @Schema(description = "체크인 시간", example = "15:00")
    LocalTime checkInTime,

    @Schema(description = "체크아웃 시간", example = "11:00")
    LocalTime checkOutTime,

    @Schema(description = "썸네일 URL")
    String thumbnailUrl,

    @Schema(description = "이미지 목록")
    List<ImageEntryResponse> images,

    @Schema(description = "객실 유형 목록")
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
            .images(mapToRoomTypeImageEntries(rt.images()))
            .build();
    }

    private static List<ImageEntryResponse> mapToRoomTypeImageEntries(List<RoomTypeImageEntryResult> images) {
        return images.stream()
            .map(PropertyDetailResponse::mapToRoomTypeImageEntry)
            .toList();
    }

    private static ImageEntryResponse mapToRoomTypeImageEntry(RoomTypeImageEntryResult img) {
        return ImageEntryResponse.builder()
            .imageUrl(img.imageUrl())
            .sortOrder(img.sortOrder())
            .build();
    }
}
