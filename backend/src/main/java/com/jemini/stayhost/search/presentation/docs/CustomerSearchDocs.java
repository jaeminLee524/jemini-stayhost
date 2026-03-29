package com.jemini.stayhost.search.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.search.presentation.dto.PropertyDetailResponse;
import com.jemini.stayhost.search.presentation.dto.PropertySearchResponse;
import com.jemini.stayhost.search.presentation.dto.RoomTypeRateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Customer 숙소 검색", description = "숙소 검색/상세/요금 조회 API (인증 불필요)")
public interface CustomerSearchDocs {

    @Operation(summary = "숙소 검색", description = "지역/이름으로 ACTIVE 숙소를 검색한다.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "검색 성공"))
    ApiBaseResponse<PageResult<PropertySearchResponse>> searchProperties(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String keyword,
        Pageable pageable
    );

    @Operation(summary = "숙소 상세 조회", description = "숙소 상세 정보와 객실 유형 목록을 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "숙소 없음")
    })
    ApiBaseResponse<PropertyDetailResponse> getPropertyDetail(
        @PathVariable Long id
    );

    @Operation(summary = "객실별 요금 조회", description = "숙소의 객실별 날짜 범위 요금과 재고 가용 여부를 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "숙소 없음")
    })
    ApiBaseResponse<RoomTypeRateResponse> getRoomTypeRates(
        @PathVariable Long id,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    );
}
