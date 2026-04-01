package com.jemini.stayhost.user.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.UserId;
import com.jemini.stayhost.user.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Customer 회원", description = "내정보 조회 API")
public interface CustomerUserDocs {

    @Operation(summary = "내 정보 조회", description = "인증된 회원의 정보를 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ApiBaseResponse<UserResponse> getMe(
        @Parameter(hidden = true) UserId userId
    );
}
