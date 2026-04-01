package com.jemini.stayhost.property.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.presentation.dto.InventoryBulkSetRequest;
import com.jemini.stayhost.property.presentation.dto.InventoryBulkSetResponse;
import com.jemini.stayhost.property.presentation.dto.InventoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Extranet 재고", description = "날짜 범위 재고 일괄 설정/조회 API")
public interface ExtranetInventoryDocs {

    @Operation(summary = "재고 일괄 설정", description = "날짜 범위 재고를 일괄 설정한다. totalCount < reservedCount인 경우 설정이 거부된다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "설정 성공"),
        @ApiResponse(responseCode = "400", description = "재고 수가 예약 건수보다 적음"),
        @ApiResponse(responseCode = "403", description = "소유권 없음"),
        @ApiResponse(responseCode = "404", description = "객실 유형 없음")
    })
    ApiBaseResponse<InventoryBulkSetResponse> bulkSet(
        @PathVariable Long id,
        @Parameter(hidden = true) PartnerId partnerId,
        @RequestBody @Valid InventoryBulkSetRequest request
    );

    @Operation(summary = "재고 조회", description = "날짜 범위 재고 목록을 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "소유권 없음"),
        @ApiResponse(responseCode = "404", description = "객실 유형 없음")
    })
    ApiBaseResponse<InventoryListResponse> getInventory(
        @PathVariable Long id,
        @Parameter(hidden = true) PartnerId partnerId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
