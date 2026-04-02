package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryEntryResult;
import com.jemini.stayhost.property.application.dto.InventoryListResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "재고 목록 응답")
public record InventoryListResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long roomTypeId,

    @Schema(description = "날짜별 재고 목록")
    List<InventoryEntryResponse> inventory
) {

    public static InventoryListResponse from(final InventoryListResult result) {
        return InventoryListResponse.builder()
            .roomTypeId(result.roomTypeId())
            .inventory(mapToInventories(result.inventory()))
            .build();
    }

    private static List<InventoryEntryResponse> mapToInventories(List<InventoryEntryResult> inventoryEntryResults) {
        return inventoryEntryResults.stream()
            .map(InventoryListResponse::mapToInventory)
            .toList();
    }

    private static InventoryEntryResponse mapToInventory(InventoryEntryResult e) {
        return InventoryEntryResponse.builder()
            .date(e.date())
            .totalCount(e.totalCount())
            .reservedCount(e.reservedCount())
            .availableCount(e.availableCount())
            .build();
    }
}
