package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.application.dto.PropertyResult;
import com.jemini.stayhost.property.application.service.PropertyService;
import com.jemini.stayhost.support.ControllerTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExtranetPropertyController.class)
class ExtranetPropertyControllerSmokeTest extends ControllerTestBase {

    @MockitoBean
    private PropertyService propertyService;

    @Test
    @DisplayName("[Smoke] 인증된 요청 시 PartnerId가 정상 주입된다")
    void smoke_인증된_요청시_PartnerId_정상_주입() throws Exception {
        final Long partnerId = 1L;
        setPartnerAuthentication(partnerId);

        final PageResult<PropertyResult> emptyPage = new PageResult<>(List.of(), 0, 20, 0, 0, false);
        given(propertyService.getMyProperties(eq(partnerId), any())).willReturn(emptyPage);

        mockMvc.perform(get("/api/extranet/properties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"));

        verify(propertyService).getMyProperties(eq(partnerId), any());
    }

    @Test
    @DisplayName("[Smoke] 인증 없이 접근 시 401 응답")
    void smoke_인증없이_접근시_401() throws Exception {
        mockMvc.perform(get("/api/extranet/properties"))
            .andExpect(status().isUnauthorized());
    }
}
