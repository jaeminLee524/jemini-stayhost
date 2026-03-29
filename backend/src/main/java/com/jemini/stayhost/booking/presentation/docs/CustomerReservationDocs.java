package com.jemini.stayhost.booking.presentation.docs;

import com.jemini.stayhost.booking.presentation.dto.*;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Customer 예약", description = "예약 생성/조회/취소 API")
public interface CustomerReservationDocs {

    @Operation(summary = "예약 생성", description = "재고 차감과 예약 확정을 원자적으로 처리한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "예약 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @ApiResponse(responseCode = "409", description = "재고 부족")
    })
    ApiBaseResponse<CreateReservationResponse> createReservation(
        @Parameter(hidden = true) UserId userId,
        @RequestBody @Valid CreateReservationRequest request
    );

    @Operation(summary = "내 예약 목록 조회", description = "인증된 회원의 예약 목록을 조회한다.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "조회 성공"))
    ApiBaseResponse<PageResult<ReservationListResponse>> getMyReservations(
        @Parameter(hidden = true) UserId userId,
        @RequestParam(required = false) String status,
        Pageable pageable
    );

    @Operation(summary = "예약 상세 조회", description = "본인 예약만 조회 가능하다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "본인 예약이 아님"),
        @ApiResponse(responseCode = "404", description = "예약 없음")
    })
    ApiBaseResponse<ReservationDetailResponse> getReservation(
        @Parameter(hidden = true) UserId userId,
        @PathVariable Long id
    );

    @Operation(summary = "예약 취소", description = "본인 예약만 취소 가능하다. 재고를 복원한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "400", description = "이미 취소된 예약"),
        @ApiResponse(responseCode = "403", description = "본인 예약이 아님")
    })
    ApiBaseResponse<CancelReservationResponse> cancelReservation(
        @Parameter(hidden = true) UserId userId,
        @PathVariable Long id,
        @RequestBody @Valid CancelReservationRequest request
    );
}
