package com.jemini.stayhost.user.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.user.presentation.dto.UserLoginRequest;
import com.jemini.stayhost.user.presentation.dto.UserLoginResponse;
import com.jemini.stayhost.user.presentation.dto.UserSignupRequest;
import com.jemini.stayhost.user.presentation.dto.UserSignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customer 인증", description = "회원가입/로그인 API")
public interface CustomerAuthDocs {

    @Operation(summary = "회원가입", description = "이메일 중복 검증 후 회원을 생성한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "가입 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    ApiBaseResponse<UserSignupResponse> signup(
        @RequestBody @Valid UserSignupRequest request
    );

    @Operation(summary = "로그인", description = "이메일/비밀번호 검증 후 JWT를 발급한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiBaseResponse<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request
    );
}
