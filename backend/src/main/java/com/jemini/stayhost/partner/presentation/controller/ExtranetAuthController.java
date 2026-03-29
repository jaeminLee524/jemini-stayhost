package com.jemini.stayhost.partner.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.partner.application.dto.PartnerLoginResult;
import com.jemini.stayhost.partner.application.service.PartnerService;
import com.jemini.stayhost.partner.presentation.dto.PartnerLoginRequest;
import com.jemini.stayhost.partner.presentation.dto.PartnerLoginResponse;
import com.jemini.stayhost.partner.presentation.docs.ExtranetAuthDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExtranetAuthController implements ExtranetAuthDocs {

    private final PartnerService partnerService;

    @PostMapping("/api/public/extranet/auth/login")
    public ApiBaseResponse<PartnerLoginResponse> login(@RequestBody @Valid final PartnerLoginRequest request) {
        final PartnerLoginResult result = partnerService.login(request.toCommand());
        return ApiBaseResponse.success(PartnerLoginResponse.from(result));
    }
}
