package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import com.jemini.stayhost.property.application.service.RateService;
import com.jemini.stayhost.property.presentation.docs.ExtranetRateDocs;
import com.jemini.stayhost.property.presentation.dto.RateBulkSetRequest;
import com.jemini.stayhost.property.presentation.dto.RateBulkSetResponse;
import com.jemini.stayhost.property.presentation.dto.RateListResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/extranet/room-types/{id}/rates")
@RequiredArgsConstructor
public class ExtranetRateController implements ExtranetRateDocs {

    private final RateService rateService;

    @PutMapping
    public ApiBaseResponse<RateBulkSetResponse> bulkSet(
        @PathVariable final Long id,
        final PartnerId partnerId,
        @RequestBody @Valid final RateBulkSetRequest request
    ) {
        RateBulkSetResult result = rateService.bulkSet(id, partnerId.value(), request.toCommand());

        return ApiBaseResponse.success(RateBulkSetResponse.from(result));
    }

    @GetMapping
    public ApiBaseResponse<RateListResponse> getRates(
        @PathVariable final Long id,
        final PartnerId partnerId,
        @RequestParam final LocalDate startDate,
        @RequestParam final LocalDate endDate
    ) {
        RateListResult result = rateService.getRates(id, partnerId.value(), startDate, endDate);

        return ApiBaseResponse.success(RateListResponse.from(result));
    }
}
