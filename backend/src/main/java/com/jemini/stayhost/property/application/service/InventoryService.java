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
    public InventoryBulkSetResult bulkSet(
        final Long roomTypeId,
        final Long partnerId,
        final InventoryBulkSetCommand command
    ) {
        validateOwnership(roomTypeId, partnerId);
        validateDateRange(command.startDate(), command.endDate());

        final List<Inventory> inventories = upsertInventories(roomTypeId, command);
        inventoryManager.saveAll(inventories);

        return buildBulkSetResult(roomTypeId, inventories.size(), command);
    }

    private void validateOwnership(final Long roomTypeId, final Long partnerId) {
        final RoomType roomType = roomTypeReader.getById(roomTypeId);
        final Property property = propertyReader.getById(roomType.getPropertyId());

        property.validateOwner(partnerId);
    }

    private void validateDateRange(final LocalDate startDate, final LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private List<Inventory> upsertInventories(final Long roomTypeId, final InventoryBulkSetCommand command) {
        final List<LocalDate> dates = generateDates(command.startDate(), command.endDate());
        final Map<LocalDate, Inventory> existing = loadExistingInventories(roomTypeId, command.startDate(), command.endDate());

        return dates.stream()
            .map(date -> upsertSingle(roomTypeId, date, command.totalCount(), existing))
            .toList();
    }

    /**
     * 날짜 범위 재고 조회.
     */
    @Transactional(readOnly = true)
    public InventoryListResult getInventory(
        final Long roomTypeId,
        final Long partnerId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        validateOwnership(roomTypeId, partnerId);

        final List<Inventory> inventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);

        return InventoryListResult.of(roomTypeId, inventories);
    }

    private Map<LocalDate, Inventory> loadExistingInventories(
        final Long roomTypeId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        return inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate)
            .stream()
            .collect(Collectors.toMap(Inventory::getDate, i -> i));
    }

    private Inventory upsertSingle(
        final Long roomTypeId,
        final LocalDate date,
        final int totalCount,
        final Map<LocalDate, Inventory> existing
    ) {
        final Inventory inventory = existing.get(date);
        if (inventory != null) {
            inventory.updateTotalCount(totalCount);
            return inventory;
        }
        return Inventory.create(roomTypeId, date, totalCount);
    }

    private InventoryBulkSetResult buildBulkSetResult(
        final Long roomTypeId,
        final int appliedDates,
        final InventoryBulkSetCommand command
    ) {
        return InventoryBulkSetResult.builder()
            .roomTypeId(roomTypeId)
            .appliedDates(appliedDates)
            .startDate(command.startDate())
            .endDate(command.endDate())
            .totalCount(command.totalCount())
            .build();
    }

    private List<LocalDate> generateDates(final LocalDate startDate, final LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }
}
