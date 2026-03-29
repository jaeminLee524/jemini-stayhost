package com.jemini.stayhost.property.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Extranet 숙소", description = "숙소 CRUD + 상태 변경 API")
public interface ExtranetPropertyDocs {

  @Operation(summary = "숙소 등록", description = "새 숙소를 등록한다. INACTIVE 상태로 생성.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "숙소 등록 성공"),
      @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
  })
  ApiBaseResponse<PropertyResponse> create(
      @Parameter(hidden = true) PartnerId partnerId,
      @RequestBody @Valid PropertyCreateRequest request
  );

  @Operation(summary = "내 숙소 목록 조회", description = "파트너 소유 숙소 목록을 페이지네이션으로 조회한다.")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "조회 성공"))
  ApiBaseResponse<PageResult<PropertyListResponse>> getMyProperties(
      @Parameter(hidden = true) PartnerId partnerId,
      Pageable pageable
  );

  @Operation(summary = "숙소 상세 조회", description = "숙소 상세 정보를 조회한다. 소유권 검증.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "소유권 없음"),
      @ApiResponse(responseCode = "404", description = "숙소 없음")
  })
  ApiBaseResponse<PropertyResponse> getProperty(
      @PathVariable Long id,
      @Parameter(hidden = true) PartnerId partnerId
  );

  @Operation(summary = "숙소 정보 수정", description = "숙소 이름, 설명, 체크인/아웃 시간, 썸네일을 수정한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "403", description = "소유권 없음"),
      @ApiResponse(responseCode = "404", description = "숙소 없음")
  })
  ApiBaseResponse<PropertyResponse> update(
      @PathVariable Long id,
      @Parameter(hidden = true) PartnerId partnerId,
      @RequestBody @Valid PropertyUpdateRequest request
  );

  @Operation(summary = "숙소 상태 변경", description = "숙소 상태를 ACTIVE/INACTIVE로 변경한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
      @ApiResponse(responseCode = "403", description = "소유권 없음"),
      @ApiResponse(responseCode = "404", description = "숙소 없음")
  })
  ApiBaseResponse<PropertyResponse> changeStatus(
      @PathVariable Long id,
      @Parameter(hidden = true) PartnerId partnerId,
      @RequestBody @Valid PropertyStatusRequest request
  );
}
