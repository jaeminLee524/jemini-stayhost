package com.jemini.stayhost.property.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.presentation.dto.RoomTypeCreateRequest;
import com.jemini.stayhost.property.presentation.dto.RoomTypeResponse;
import com.jemini.stayhost.property.presentation.dto.RoomTypeUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Extranet 객실 유형", description = "객실 유형 CRUD API")
public interface ExtranetRoomTypeDocs {

    @Operation(summary = "객실 유형 등록", description = "숙소에 새 객실 유형을 등록한다. ACTIVE 상태로 생성.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "객실 유형 등록 성공"),
        @ApiResponse(responseCode = "403", description = "숙소 소유권 없음"),
        @ApiResponse(responseCode = "404", description = "숙소 없음")
    })
    ApiBaseResponse<RoomTypeResponse> create(
        @PathVariable Long propertyId,
        @Parameter(hidden = true) PartnerId partnerId,
        @RequestBody @Valid RoomTypeCreateRequest request
    );

    @Operation(summary = "객실 유형 목록 조회", description = "숙소의 전체 객실 유형 목록을 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "숙소 소유권 없음"),
        @ApiResponse(responseCode = "404", description = "숙소 없음")
    })
    ApiBaseResponse<List<RoomTypeResponse>> getRoomTypes(
        @PathVariable Long propertyId,
        @Parameter(hidden = true) PartnerId partnerId
    );

    @Operation(summary = "객실 유형 수정", description = "객실 유형 정보를 수정한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "403", description = "숙소 소유권 없음"),
        @ApiResponse(responseCode = "404", description = "객실 유형 없음")
    })
    ApiBaseResponse<RoomTypeResponse> update(
        @PathVariable Long id,
        @Parameter(hidden = true) PartnerId partnerId,
        @RequestBody @Valid RoomTypeUpdateRequest request
    );
}
