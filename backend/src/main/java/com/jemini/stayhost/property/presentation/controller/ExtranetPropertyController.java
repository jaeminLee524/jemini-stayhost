package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.application.dto.PropertyResult;
import com.jemini.stayhost.property.application.service.PropertyService;
import com.jemini.stayhost.property.domain.model.PropertyStatus;
import com.jemini.stayhost.property.presentation.dto.*;
import com.jemini.stayhost.property.presentation.docs.ExtranetPropertyDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ExtranetPropertyController implements ExtranetPropertyDocs {

  private final PropertyService propertyService;

  @PostMapping("/api/extranet/properties")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiBaseResponse<PropertyResponse> create(
      final PartnerId partnerId,
      @RequestBody @Valid final PropertyCreateRequest request
  ) {
    final PropertyResult result = propertyService.createProperty(partnerId.value(), request.toCommand());
    return ApiBaseResponse.success(PropertyResponse.from(result));
  }

  @GetMapping("/api/extranet/properties")
  public ApiBaseResponse<PageResult<PropertyListResponse>> getMyProperties(
      final PartnerId partnerId,
      final Pageable pageable
  ) {
    final PageResult<PropertyListResponse> page = propertyService.getMyProperties(partnerId.value(), pageable)
        .map(PropertyListResponse::from);
    return ApiBaseResponse.success(page);
  }

  @GetMapping("/api/extranet/properties/{id}")
  public ApiBaseResponse<PropertyResponse> getProperty(
      @PathVariable final Long id,
      final PartnerId partnerId
  ) {
    final PropertyResult result = propertyService.getProperty(id, partnerId.value());
    return ApiBaseResponse.success(PropertyResponse.from(result));
  }

  @PutMapping("/api/extranet/properties/{id}")
  public ApiBaseResponse<PropertyResponse> update(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestBody @Valid final PropertyUpdateRequest request
  ) {
    final PropertyResult result = propertyService.updateProperty(id, partnerId.value(), request.toCommand());
    return ApiBaseResponse.success(PropertyResponse.from(result));
  }

  @PatchMapping("/api/extranet/properties/{id}/status")
  public ApiBaseResponse<PropertyResponse> changeStatus(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestBody @Valid final PropertyStatusRequest request
  ) {
    final PropertyResult result = propertyService.changeStatus(id, partnerId.value(), PropertyStatus.valueOf(request.status()));
    return ApiBaseResponse.success(PropertyResponse.from(result));
  }
}
