package com.jemini.stayhost.booking.presentation.controller;

import com.jemini.stayhost.booking.application.dto.CancelReservationResult;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.application.facade.ReservationFacade;
import com.jemini.stayhost.booking.application.service.ReservationService;
import com.jemini.stayhost.booking.presentation.docs.CustomerReservationDocs;
import com.jemini.stayhost.booking.presentation.dto.*;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class CustomerReservationController implements CustomerReservationDocs {

    private final ReservationFacade reservationFacade;
    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiBaseResponse<CreateReservationResponse> createReservation(
        final UserId userId,
        @RequestBody @Valid final CreateReservationRequest request
    ) {
        final ReservationResult result = reservationFacade.createReservation(userId.value(), request.toCommand());

        return ApiBaseResponse.success(CreateReservationResponse.from(result));
    }

    @GetMapping
    public ApiBaseResponse<PageResult<ReservationListResponse>> getMyReservations(
        final UserId userId,
        @RequestParam(required = false) final String status,
        final Pageable pageable
    ) {
        final PageResult<ReservationResult> result = reservationService.getMyReservations(userId.value(), status, pageable);

        return ApiBaseResponse.success(result.map(ReservationListResponse::from));
    }

    @GetMapping("/{id}")
    public ApiBaseResponse<ReservationDetailResponse> getReservation(
        final UserId userId,
        @PathVariable final Long id
    ) {
        final ReservationResult result = reservationService.getReservation(id, userId.value());

        return ApiBaseResponse.success(ReservationDetailResponse.from(result));
    }

    @PostMapping("/{id}/cancel")
    public ApiBaseResponse<CancelReservationResponse> cancelReservation(
        final UserId userId,
        @PathVariable final Long id,
        @RequestBody @Valid final CancelReservationRequest request
    ) {
        final CancelReservationResult result = reservationService.cancelReservation(id, userId.value(), request.cancelReason());

        return ApiBaseResponse.success(CancelReservationResponse.from(result));
    }
}
