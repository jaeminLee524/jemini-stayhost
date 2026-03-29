package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetCommand;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;
import com.jemini.stayhost.property.application.dto.InventoryListResult;
import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final RoomTypeReader roomTypeReader;
  private final PropertyReader propertyReader;
  private final InventoryReader inventoryReader;
  private final InventoryManager inventoryManager;

  /**
   * 날짜 범위 재고 일괄 설정. totalCount < reservedCount인 경우 설정이 거부된다.
   */
  @Transactional
  public InventoryBulkSetResult bulkSet(final Long roomTypeId, final Long partnerId, final InventoryBulkSetCommand command) {
    final RoomType roomType = roomTypeReader.getById(roomTypeId);
    validateRoomTypeOwner(roomType, partnerId);

    final List<LocalDate> dates = generateDates(command.startDate(), command.endDate());
    final Map<LocalDate, Inventory> existing = inventoryReader
        .findByRoomTypeIdAndDateBetween(roomTypeId, command.startDate(), command.endDate())
        .stream().collect(Collectors.toMap(Inventory::getDate, i -> i));

    final List<Inventory> inventories = dates.stream()
        .map(date -> {
          final Inventory inventory = existing.get(date);
          if (inventory != null) {
            if (command.totalCount() < inventory.getReservedCount()) {
              throw new BusinessException(ErrorCode.INVENTORY_TOTAL_BELOW_RESERVED);
            }
            inventory.updateTotalCount(command.totalCount());
            return inventory;
          }
          return Inventory.create(roomTypeId, date, command.totalCount());
        })
        .toList();

    inventoryManager.saveAll(inventories);

    return InventoryBulkSetResult.builder()
        .roomTypeId(roomTypeId)
        .appliedDates(inventories.size())
        .startDate(command.startDate())
        .endDate(command.endDate())
        .totalCount(command.totalCount())
        .build();
  }

  /**
   * 날짜 범위 재고 조회.
   */
  @Transactional(readOnly = true)
  public InventoryListResult getInventory(final Long roomTypeId, final Long partnerId, final LocalDate startDate, final LocalDate endDate) {
    final RoomType roomType = roomTypeReader.getById(roomTypeId);
    validateRoomTypeOwner(roomType, partnerId);

    final List<Inventory> inventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);

    return InventoryListResult.of(roomTypeId, inventories);
  }

  private void validateRoomTypeOwner(final RoomType roomType, final Long partnerId) {
    final Property property = propertyReader.getById(roomType.getPropertyId());
    property.validateOwner(partnerId);
  }

  private List<LocalDate> generateDates(final LocalDate startDate, final LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1)).toList();
  }
}
