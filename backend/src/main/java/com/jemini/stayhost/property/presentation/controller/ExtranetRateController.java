package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.application.service.RateService;
import com.jemini.stayhost.property.presentation.docs.ExtranetRateDocs;
import com.jemini.stayhost.property.presentation.dto.RateBulkSetRequest;
import com.jemini.stayhost.property.presentation.dto.RateBulkSetResponse;
import com.jemini.stayhost.property.presentation.dto.RateListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ExtranetRateController implements ExtranetRateDocs {

  private final RateService rateService;

  @PutMapping("/api/extranet/room-types/{id}/rates")
  public ApiBaseResponse<RateBulkSetResponse> bulkSet(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestBody @Valid final RateBulkSetRequest request
  ) {
    return ApiBaseResponse.success(
        RateBulkSetResponse.from(rateService.bulkSet(id, partnerId.value(), request.toCommand()))
    );
  }

  @GetMapping("/api/extranet/room-types/{id}/rates")
  public ApiBaseResponse<RateListResponse> getRates(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate
  ) {
    return ApiBaseResponse.success(
        RateListResponse.from(rateService.getRates(id, partnerId.value(), startDate, endDate))
    );
  }
}
