package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.application.dto.RoomTypeResult;
import com.jemini.stayhost.property.application.service.RoomTypeService;
import com.jemini.stayhost.property.presentation.docs.ExtranetRoomTypeDocs;
import com.jemini.stayhost.property.presentation.dto.RoomTypeCreateRequest;
import com.jemini.stayhost.property.presentation.dto.RoomTypeResponse;
import com.jemini.stayhost.property.presentation.dto.RoomTypeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extranet")
@RequiredArgsConstructor
public class ExtranetRoomTypeController implements ExtranetRoomTypeDocs {

    private final RoomTypeService roomTypeService;

    @PostMapping("/properties/{propertyId}/room-types")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiBaseResponse<RoomTypeResponse> create(
        @PathVariable final Long propertyId,
        final PartnerId partnerId,
        @RequestBody @Valid final RoomTypeCreateRequest request
    ) {
        final RoomTypeResult result = roomTypeService.createRoomType(propertyId, partnerId.value(), request.toCommand());

        return ApiBaseResponse.success(RoomTypeResponse.from(result));
    }

    @GetMapping("/properties/{propertyId}/room-types")
    public ApiBaseResponse<List<RoomTypeResponse>> getRoomTypes(
        @PathVariable final Long propertyId,
        final PartnerId partnerId
    ) {
        List<RoomTypeResult> roomTypes = roomTypeService.getRoomTypes(propertyId, partnerId.value());

        return ApiBaseResponse.success(RoomTypeResponse.mapToResponse(roomTypes));
    }

    @PutMapping("/room-types/{id}")
    public ApiBaseResponse<RoomTypeResponse> update(
        @PathVariable final Long id,
        final PartnerId partnerId,
        @RequestBody @Valid final RoomTypeUpdateRequest request
    ) {
        final RoomTypeResult result = roomTypeService.updateRoomType(id, partnerId.value(), request.toCommand());

        return ApiBaseResponse.success(RoomTypeResponse.from(result));
    }
}
