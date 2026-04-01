package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.PropertyResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "숙소 목록 항목")
public record PropertyListResponse(
    @Schema(description = "숙소 ID", example = "100")
    Long id,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String name,

    @Schema(description = "지역", example = "서울")
    String region,

    @Schema(description = "숙소 유형", example = "HOTEL")
    String type,

    @Schema(description = "상태", example = "ACTIVE")
    String status,

    @Schema(description = "썸네일 URL")
    String thumbnailUrl
) {

    public static PropertyListResponse from(final PropertyResult result) {
        return PropertyListResponse.builder()
            .id(result.id())
            .name(result.name())
            .region(result.region())
            .type(result.type())
            .status(result.status())
            .thumbnailUrl(result.thumbnailUrl())
            .build();
    }
}
