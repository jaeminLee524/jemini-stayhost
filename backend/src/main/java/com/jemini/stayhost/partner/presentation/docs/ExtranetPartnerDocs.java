package com.jemini.stayhost.partner.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.partner.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Extranet 파트너", description = "파트너 등록/조회/수정 API")
public interface ExtranetPartnerDocs {

    @Operation(summary = "파트너 등록", description = """
        신규 파트너를 등록한다. 인증 불필요.
        등록 직후 PENDING 상태이며, 관리자 승인 후 ACTIVE로 전환된다.
        ``` json
        [ERROR_CODE]
        * DUPLICATE_LOGIN_ID: 이미 사용 중인 로그인 아이디
        * DUPLICATE_BUSINESS_NUMBER: 이미 등록된 사업자번호
        ```
        """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "파트너 등록 성공"),
        @ApiResponse(responseCode = "409", description = "중복 데이터")
    })
    ApiBaseResponse<PartnerRegisterResponse> register(
        @RequestBody @Valid PartnerRegisterRequest request
    );

    @Operation(summary = "내 파트너 정보 조회", description = "JWT 토큰 기반으로 내 파트너 정보를 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "파트너 없음")
    })
    ApiBaseResponse<PartnerResponse> getMyPartner(
        @Parameter(hidden = true) PartnerId partnerId
    );

    @Operation(summary = "파트너 정보 수정", description = "연락처, 이메일, 정산 계좌 정보를 수정한다. 사업자번호는 수정 불가.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "파트너 없음")
    })
    ApiBaseResponse<PartnerResponse> updateMyPartner(
        @Parameter(hidden = true) PartnerId partnerId,
        @RequestBody @Valid PartnerUpdateRequest request
    );
}
