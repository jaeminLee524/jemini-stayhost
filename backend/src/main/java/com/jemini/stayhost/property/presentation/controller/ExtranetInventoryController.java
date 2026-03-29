package com.jemini.stayhost.property.presentation.controller;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import com.jemini.stayhost.common.security.PartnerId;
import com.jemini.stayhost.property.application.service.InventoryService;
import com.jemini.stayhost.property.presentation.docs.ExtranetInventoryDocs;
import com.jemini.stayhost.property.presentation.dto.InventoryBulkSetRequest;
import com.jemini.stayhost.property.presentation.dto.InventoryBulkSetResponse;
import com.jemini.stayhost.property.presentation.dto.InventoryListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ExtranetInventoryController implements ExtranetInventoryDocs {

  private final InventoryService inventoryService;

  @PutMapping("/api/extranet/room-types/{id}/inventory")
  public ApiBaseResponse<InventoryBulkSetResponse> bulkSet(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestBody @Valid final InventoryBulkSetRequest request
  ) {
    return ApiBaseResponse.success(
        InventoryBulkSetResponse.from(inventoryService.bulkSet(id, partnerId.value(), request.toCommand()))
    );
  }

  @GetMapping("/api/extranet/room-types/{id}/inventory")
  public ApiBaseResponse<InventoryListResponse> getInventory(
      @PathVariable final Long id,
      final PartnerId partnerId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate
  ) {
    return ApiBaseResponse.success(
        InventoryListResponse.from(inventoryService.getInventory(id, partnerId.value(), startDate, endDate))
    );
  }
}
