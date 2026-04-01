package com.jemini.stayhost.search.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import com.jemini.stayhost.search.application.service.SearchService;
import com.jemini.stayhost.search.presentation.docs.CustomerSearchDocs;
import com.jemini.stayhost.search.presentation.dto.PropertyDetailResponse;
import com.jemini.stayhost.search.presentation.dto.PropertySearchResponse;
import com.jemini.stayhost.search.presentation.dto.RoomTypeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class CustomerSearchController implements CustomerSearchDocs {

    private final SearchService searchService;

    @GetMapping("/search/properties")
    public ApiBaseResponse<PageResult<PropertySearchResponse>> searchProperties(
        @RequestParam(required = false) final String region,
        @RequestParam(required = false) final String keyword,
        final Pageable pageable
    ) {
        final PageResult<PropertySearchResult> result = searchService.searchProperties(region, keyword, pageable);

        return ApiBaseResponse.success(result.map(PropertySearchResponse::from));
    }

    @GetMapping("/properties/{id}")
    public ApiBaseResponse<PropertyDetailResponse> getPropertyDetail(
        @PathVariable final Long id
    ) {
        final PropertyDetailResult result = searchService.getPropertyDetail(id);

        return ApiBaseResponse.success(PropertyDetailResponse.from(result));
    }

    @GetMapping("/search/properties/{id}/rates")
    public ApiBaseResponse<RoomTypeRateResponse> getRoomTypeRates(
        @PathVariable final Long id,
        @RequestParam final LocalDate startDate,
        @RequestParam final LocalDate endDate
    ) {
        final RoomTypeRateResult result = searchService.getRoomTypeRates(id, startDate, endDate);

        return ApiBaseResponse.success(RoomTypeRateResponse.from(result));
    }
}
