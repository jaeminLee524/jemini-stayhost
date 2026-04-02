package com.jemini.stayhost.booking.presentation.docs;

import com.jemini.stayhost.booking.application.dto.ExtranetReservationSearch;
import com.jemini.stayhost.booking.presentation.dto.ExtranetReservationDetailResponse;
import com.jemini.stayhost.booking.presentation.dto.ExtranetReservationListResponse;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.PartnerId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Extranet 예약", description = "파트너 예약 조회 API")
public interface ExtranetReservationDocs {

    @Operation(summary = "예약 목록 조회", description = "파트너가 소유한 숙소의 예약 목록을 조회한다.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "조회 성공"))
    ApiBaseResponse<PageResult<ExtranetReservationListResponse>> getReservations(
        @Parameter(hidden = true) PartnerId partnerId,
        ExtranetReservationSearch search,
        Pageable pageable
    );

    @Operation(summary = "예약 상세 조회", description = "파트너 소유 숙소의 예약 상세 정보를 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "소유 숙소의 예약이 아님"),
        @ApiResponse(responseCode = "404", description = "예약 없음")
    })
    ApiBaseResponse<ExtranetReservationDetailResponse> getReservation(
        @Parameter(hidden = true) PartnerId partnerId,
        @PathVariable Long id
    );
}
