package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Builder
@Schema(description = "숙소 검색 결과 항목")
public record PropertySearchResponse(
    @Schema(description = "숙소 ID", example = "100")
    Long id,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String name,

    @Schema(description = "숙소 유형", example = "HOTEL")
    String type,

    @Schema(description = "지역", example = "서울")
    String region,

    @Schema(description = "주소")
    String address,

    @Schema(description = "썸네일 URL")
    String thumbnailUrl,

    @Schema(description = "체크인 시간", example = "15:00")
    LocalTime checkInTime,

    @Schema(description = "체크아웃 시간", example = "11:00")
    LocalTime checkOutTime,

    @Schema(description = "최저 요금", example = "80000")
    BigDecimal minPrice,

    @Schema(description = "예약 가능 객실 유형 수", example = "3")
    int availableRoomTypes
) {

    public static PropertySearchResponse from(final PropertySearchResult result) {
        return PropertySearchResponse.builder()
            .id(result.id())
            .name(result.name())
            .type(result.type())
            .region(result.region())
            .address(result.address())
            .thumbnailUrl(result.thumbnailUrl())
            .checkInTime(result.checkInTime())
            .checkOutTime(result.checkOutTime())
            .minPrice(result.minPrice())
            .availableRoomTypes(result.availableRoomTypes())
            .build();
    }
}
