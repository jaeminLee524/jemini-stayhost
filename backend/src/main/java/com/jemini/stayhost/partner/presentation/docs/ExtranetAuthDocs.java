package com.jemini.stayhost.partner.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.partner.presentation.dto.PartnerLoginRequest;
import com.jemini.stayhost.partner.presentation.dto.PartnerLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Extranet 인증", description = "파트너 로그인 API")
public interface ExtranetAuthDocs {

  @Operation(summary = "파트너 로그인", description = """
      로그인 아이디/비밀번호로 인증 후 JWT를 발급한다.
      ``` json
      [ERROR_CODE]
      * PARTNER_NOT_FOUND: 로그인 아이디에 해당하는 파트너 없음
      * UNAUTHORIZED: 비밀번호 불일치
      * FORBIDDEN: 활성 상태가 아닌 파트너 (PENDING/SUSPENDED)
      ```
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 발급"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "파트너 없음")
  })
  ApiBaseResponse<PartnerLoginResponse> login(@RequestBody @Valid PartnerLoginRequest request);
}
