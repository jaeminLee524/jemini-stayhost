package com.jemini.stayhost.partner.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.partner.application.dto.PartnerResult;
import com.jemini.stayhost.partner.application.service.PartnerService;
import com.jemini.stayhost.partner.presentation.dto.*;
import com.jemini.stayhost.partner.presentation.docs.ExtranetPartnerDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ExtranetPartnerController implements ExtranetPartnerDocs {

    private final PartnerService partnerService;

    @PostMapping("/api/extranet/partners")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiBaseResponse<PartnerRegisterResponse> register(
        @RequestBody @Valid final PartnerRegisterRequest request
    ) {
        final PartnerResult result = partnerService.register(request.toCommand());
        return ApiBaseResponse.success(PartnerRegisterResponse.from(result));
    }

    @GetMapping("/api/extranet/partners/me")
    public ApiBaseResponse<PartnerResponse> getMyPartner(
        final PartnerId partnerId
    ) {
        final PartnerResult result = partnerService.getPartner(partnerId.value());
        return ApiBaseResponse.success(PartnerResponse.from(result));
    }

    @PutMapping("/api/extranet/partners/me")
    public ApiBaseResponse<PartnerResponse> updateMyPartner(
        final PartnerId partnerId,
        @RequestBody @Valid final PartnerUpdateRequest request
    ) {
        final PartnerResult result = partnerService.updatePartner(partnerId.value(), request.toCommand());
        return ApiBaseResponse.success(PartnerResponse.from(result));
    }
}
