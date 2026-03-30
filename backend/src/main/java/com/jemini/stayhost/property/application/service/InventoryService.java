package com.jemini.stayhost.property.application.service;

import static com.jemini.stayhost.common.util.DateUtil.dateRangeInclusive;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetCommand;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;
import com.jemini.stayhost.property.application.dto.InventoryListResult;
import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.event.InventoryChangedEvent;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jemini.stayhost.common.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    private final RoomTypeReader roomTypeReader;
    private final PropertyReader propertyReader;
    private final InventoryReader inventoryReader;
    private final InventoryManager inventoryManager;
    private final ApplicationEventPublisher eventPublisher;

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

        final int appliedCount = upsertInventories(roomTypeId, command);

        final List<LocalDate> affectedDates = dateRangeInclusive(command.startDate(), command.endDate());
        eventPublisher.publishEvent(InventoryChangedEvent.create(roomTypeId, affectedDates));

        return buildBulkSetResult(roomTypeId, appliedCount, command);
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
        validateDateRange(startDate, endDate);

        final List<Inventory> inventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);

        return InventoryListResult.of(roomTypeId, inventories);
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
        final long days = DateUtil.dayCountInclusive(startDate, endDate);
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG);
        }
    }

    private int upsertInventories(final Long roomTypeId, final InventoryBulkSetCommand command) {
        final List<LocalDate> dates = dateRangeInclusive(command.startDate(), command.endDate());
        final Map<LocalDate, Inventory> existing = loadExistingInventories(roomTypeId, command.startDate(), command.endDate());

        final List<Inventory> newInventories = new ArrayList<>();

        for (LocalDate date : dates) {
            final Inventory inventory = existing.get(date);
            if (inventory != null) {
                inventory.updateTotalCount(command.totalCount());
            } else {
                newInventories.add(Inventory.create(roomTypeId, date, command.totalCount()));
            }
        }

        if (!newInventories.isEmpty()) {
            inventoryManager.saveAll(newInventories);
        }

        return dates.size();
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
}
