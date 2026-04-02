package com.jemini.stayhost.booking.presentation.controller;

import com.jemini.stayhost.booking.application.dto.ExtranetReservationSearch;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.application.service.ExtranetReservationService;
import com.jemini.stayhost.booking.presentation.docs.ExtranetReservationDocs;
import com.jemini.stayhost.booking.presentation.dto.ExtranetReservationDetailResponse;
import com.jemini.stayhost.booking.presentation.dto.ExtranetReservationListResponse;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.PartnerId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extranet/reservations")
@RequiredArgsConstructor
public class ExtranetReservationController implements ExtranetReservationDocs {

    private final ExtranetReservationService extranetReservationService;

    @GetMapping
    public ApiBaseResponse<PageResult<ExtranetReservationListResponse>> getReservations(
        final PartnerId partnerId,
        final ExtranetReservationSearch search,
        final Pageable pageable
    ) {
        final PageResult<ReservationResult> result = extranetReservationService.getReservations(
            partnerId.value(), search, pageable);

        return ApiBaseResponse.success(result.map(ExtranetReservationListResponse::from));
    }

    @GetMapping("/{id}")
    public ApiBaseResponse<ExtranetReservationDetailResponse> getReservation(
        final PartnerId partnerId,
        @PathVariable final Long id
    ) {
        final ReservationResult result = extranetReservationService.getReservation(id, partnerId.value());

        return ApiBaseResponse.success(ExtranetReservationDetailResponse.from(result));
    }
}
