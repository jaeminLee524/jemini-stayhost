package com.jemini.stayhost.supplier.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.event.InventoryChangedEvent;
import com.jemini.stayhost.property.domain.event.RateUpdatedEvent;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.supplier.domain.component.SupplierAdapter;
import com.jemini.stayhost.supplier.domain.component.SupplierManager;
import com.jemini.stayhost.supplier.domain.component.SupplierMappingReader;
import com.jemini.stayhost.supplier.domain.component.SupplierPropertyReader;
import com.jemini.stayhost.supplier.domain.component.SupplierReader;
import com.jemini.stayhost.supplier.domain.dto.SupplierInventoryData;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import com.jemini.stayhost.supplier.domain.dto.SupplierRateData;
import com.jemini.stayhost.supplier.domain.model.MappingStatus;
import com.jemini.stayhost.supplier.domain.model.Supplier;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import com.jemini.stayhost.supplier.domain.model.SyncJobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierSyncService {

    private static final int RATE_SYNC_DAYS = 30;

    private final SupplierReader supplierReader;
    private final SupplierPropertyReader supplierPropertyReader;
    private final SupplierMappingReader supplierMappingReader;
    private final SupplierManager supplierManager;
    private final RoomTypeReader roomTypeReader;
    private final RateReader rateReader;
    private final RateManager rateManager;
    private final InventoryReader inventoryReader;
    private final InventoryManager inventoryManager;
    private final ApplicationEventPublisher eventPublisher;
    private final List<SupplierAdapter> adapters;

    /**
     * 공급사 전체 동기화. 숙소 목록 → 요금/재고 순서로 동기화한다.
     */
    @Transactional
    public void syncSupplier(final Long supplierId) {
        final Supplier supplier = supplierReader.getById(supplierId);
        final SupplierAdapter adapter = findAdapter(supplier.getCode());
        final SupplierSyncJob syncJob = supplierManager.saveSyncJob(SupplierSyncJob.start(supplierId, SyncJobType.FULL_SYNC));

        try {
            syncProperties(syncJob, supplierId, adapter);
            syncRatesAndInventory(supplierId, adapter);
        } catch (final Exception e) {
            log.error("공급사 동기화 실패: supplierId={}", supplierId, e);
            syncJob.fail(e.getMessage());
            supplierManager.saveSyncJob(syncJob);
        }
    }

    /** 공급사 코드로 등록된 SupplierAdapter 구현체를 찾는다. */
    private SupplierAdapter findAdapter(final String supplierCode) {
        return adapters.stream()
            .filter(adapter -> adapter.getSupplierCode().equals(supplierCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_ADAPTER_NOT_FOUND));
    }

    /** 공급사 숙소 목록을 조회하여 개별 upsert 후 SyncJob 결과를 기록한다. */
    private void syncProperties(final SupplierSyncJob syncJob, final Long supplierId, final SupplierAdapter adapter) {
        final List<SupplierPropertyData> properties = adapter.fetchProperties();
        int successCount = 0;
        int failCount = 0;

        for (final SupplierPropertyData data : properties) {
            try {
                upsertSupplierProperty(supplierId, data);
                successCount++;
            } catch (final Exception e) {
                log.warn("공급사 숙소 동기화 실패: supplierId={}, externalId={}", supplierId, data.externalPropertyId(), e);
                failCount++;
            }
        }

        syncJob.complete(properties.size(), successCount, failCount);
        supplierManager.saveSyncJob(syncJob);
    }

    /** 공급사 숙소를 저장하거나 기존 데이터를 갱신한다. */
    private void upsertSupplierProperty(final Long supplierId, final SupplierPropertyData data) {
        supplierPropertyReader.findBySupplierIdAndExternalPropertyId(supplierId, data.externalPropertyId())
            .ifPresentOrElse(
                property -> property.updateRawData(data.rawData()),
                () -> supplierManager.saveProperty(SupplierProperty.create(supplierId, data.externalPropertyId(), data.rawData()))
            );
    }

    /**
     * MAPPED 상태인 공급사 숙소의 요금/재고를 내부 테이블에 동기화한다.
     * 매핑된 내부 숙소의 첫 번째 객실 유형에 요금/재고를 반영한다.
     */
    private void syncRatesAndInventory(final Long supplierId, final SupplierAdapter adapter) {
        final List<SupplierProperty> supplierProperties = supplierPropertyReader.findBySupplierId(supplierId);
        final List<SupplierPropertyMapping> mappedEntries = findMappedEntries(supplierProperties);

        if (mappedEntries.isEmpty()) {
            log.info("MAPPED 공급사 숙소 없음 — 요금/재고 동기화 건너뜀: supplierId={}", supplierId);
            return;
        }

        final Map<Long, SupplierProperty> supplierPropertyMap = supplierProperties.stream()
            .collect(toMap(SupplierProperty::getId, sp -> sp));
        final LocalDate from = LocalDate.now();
        final LocalDate to = from.plusDays(RATE_SYNC_DAYS);

        for (final SupplierPropertyMapping mapping : mappedEntries) {
            syncMappedProperty(adapter, mapping, supplierPropertyMap, from, to);
        }
    }

    /** MAPPED 상태인 공급사-내부 숙소 매핑 목록을 조회한다. */
    private List<SupplierPropertyMapping> findMappedEntries(final List<SupplierProperty> supplierProperties) {
        final List<Long> supplierPropertyIds = supplierProperties.stream()
            .map(SupplierProperty::getId)
            .toList();
        return supplierMappingReader.findBySupplierPropertyIds(supplierPropertyIds, MappingStatus.MAPPED);
    }

    /** 개별 매핑 숙소의 요금/재고를 동기화한다. 실패 시 로그만 남기고 다음 숙소로 진행한다. */
    private void syncMappedProperty(
        final SupplierAdapter adapter,
        final SupplierPropertyMapping mapping,
        final Map<Long, SupplierProperty> supplierPropertyMap,
        final LocalDate from,
        final LocalDate to
    ) {
        try {
            final SupplierProperty sp = supplierPropertyMap.get(mapping.getSupplierPropertyId());
            final RoomType targetRoomType = findTargetRoomType(mapping.getPropertyId());
            if (targetRoomType == null) {
                return;
            }

            syncRates(adapter, sp.getExternalPropertyId(), targetRoomType, from, to);
            syncInventory(adapter, sp.getExternalPropertyId(), targetRoomType, from, to);

            log.info("공급사 요금/재고 동기화 완료: externalPropertyId={}, propertyId={}, roomTypeId={}", sp.getExternalPropertyId(), mapping.getPropertyId(), targetRoomType.getId());
        } catch (final Exception e) {
            log.warn("공급사 요금/재고 동기화 실패: supplierPropertyId={}, propertyId={}", mapping.getSupplierPropertyId(), mapping.getPropertyId(), e);
        }
    }

    /** 내부 숙소의 첫 번째 활성 객실 유형을 반환한다. 없으면 null. */
    private RoomType findTargetRoomType(final Long propertyId) {
        final List<RoomType> roomTypes = roomTypeReader.findActiveByPropertyId(propertyId);
        if (roomTypes.isEmpty()) {
            log.debug("내부 숙소에 활성 객실 없음 — 건너뜀: propertyId={}", propertyId);
            return null;
        }
        return roomTypes.getFirst();
    }

    /** 공급사 요금 데이터를 조회하여 내부 rate 테이블에 upsert한다. */
    private void syncRates(
        final SupplierAdapter adapter,
        final String externalPropertyId,
        final RoomType roomType,
        final LocalDate from,
        final LocalDate to
    ) {
        final List<SupplierRateData> supplierRates = adapter.fetchRates(externalPropertyId, from, to);
        final Map<LocalDate, Rate> existingRates = rateReader.findByRoomTypeIdAndDateBetween(roomType.getId(), from, to).stream()
            .collect(toMap(Rate::getDate, r -> r));

        final List<Rate> newRates = new ArrayList<>();
        for (final SupplierRateData data : supplierRates) {
            final Rate existing = existingRates.get(data.date());
            if (existing != null) {
                existing.updatePrice(data.price());
            } else {
                newRates.add(Rate.create(roomType.getId(), data.date(), data.price()));
            }
        }

        if (!newRates.isEmpty()) {
            rateManager.saveAll(newRates);
        }

        publishIfNotEmpty(
            supplierRates.stream().map(SupplierRateData::date).toList(),
            dates -> eventPublisher.publishEvent(RateUpdatedEvent.create(roomType.getId(), dates))
        );
    }

    /** 공급사 재고 데이터를 조회하여 내부 inventory 테이블에 upsert한다. */
    private void syncInventory(
        final SupplierAdapter adapter,
        final String externalPropertyId,
        final RoomType roomType,
        final LocalDate from,
        final LocalDate to
    ) {
        final List<SupplierInventoryData> supplierInventories = adapter.fetchInventory(externalPropertyId, from, to);
        final Map<LocalDate, Inventory> existingInventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomType.getId(), from, to).stream()
            .collect(toMap(Inventory::getDate, i -> i));

        final List<Inventory> newInventories = new ArrayList<>();
        for (final SupplierInventoryData data : supplierInventories) {
            final Inventory existing = existingInventories.get(data.date());
            if (existing != null) {
                existing.updateTotalCount(data.availableCount());
            } else {
                newInventories.add(Inventory.create(roomType.getId(), data.date(), data.availableCount()));
            }
        }

        if (!newInventories.isEmpty()) {
            inventoryManager.saveAll(newInventories);
        }

        publishIfNotEmpty(
            supplierInventories.stream().map(SupplierInventoryData::date).toList(),
            dates -> eventPublisher.publishEvent(InventoryChangedEvent.create(roomType.getId(), dates))
        );
    }

    /** 변경된 날짜가 있을 때만 도메인 이벤트를 발행한다. */
    private void publishIfNotEmpty(final List<LocalDate> dates, final java.util.function.Consumer<List<LocalDate>> publisher) {
        if (!dates.isEmpty()) {
            publisher.accept(dates);
        }
    }
}
