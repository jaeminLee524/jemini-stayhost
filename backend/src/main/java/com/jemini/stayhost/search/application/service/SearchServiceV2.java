package com.jemini.stayhost.search.application.service;

import static com.jemini.stayhost.common.util.DateUtil.dayCountInclusive;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.model.*;
import com.jemini.stayhost.search.application.dto.DailyRateResult;
import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertyImageEntryResult;
import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import com.jemini.stayhost.search.application.dto.RoomTypeEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import com.jemini.stayhost.search.domain.component.InventoryReaderV2;
import com.jemini.stayhost.search.domain.component.PropertyReaderV2;
import com.jemini.stayhost.search.domain.component.RateReaderV2;
import com.jemini.stayhost.search.domain.component.RoomTypeReaderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceV2 {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    private final PropertyReaderV2 propertyReader;
    private final RoomTypeReaderV2 roomTypeReader;
    private final RateReaderV2 rateReader;
    private final InventoryReaderV2 inventoryReader;

    /**
     * 숙소 검색. ACTIVE 상태의 숙소만 반환한다.
     * N+1 방지: 검색 결과의 모든 숙소 객실을 한 번의 IN 쿼리로 일괄 조회한다.
     */
    public PageResult<PropertySearchResult> searchProperties(
            final String region,
            final String keyword,
            final Pageable pageable
    ) {
        final Page<Property> properties = propertyReader.searchActive(region, keyword, pageable);
        final List<Long> propertyIds = properties.getContent().stream().map(Property::getId).toList();
        final Map<Long, List<RoomType>> roomTypesByProperty = roomTypeReader.findActiveByPropertyIds(propertyIds)
            .stream()
            .collect(groupingBy(RoomType::getPropertyId));

        final List<PropertySearchResult> results = properties.getContent().stream()
            .map(property -> toSearchResult(property, roomTypesByProperty.getOrDefault(property.getId(), List.of())))
            .toList();

        return PageResult.from(new PageImpl<>(results, pageable, properties.getTotalElements()));
    }

    /**
     * 숙소 상세 조회. 객실 유형 목록 포함.
     */
    public PropertyDetailResult getPropertyDetail(final Long propertyId) {
        final Property property = propertyReader.getActiveById(propertyId);
        final List<RoomType> roomTypes = roomTypeReader.findActiveByPropertyId(propertyId);

        return buildDetailResult(property, roomTypes);
    }

    /**
     * 숙소의 객실별 날짜 범위 요금 조회.
     * per-roomType warm-up: bulk 쿼리로 조회 후 per-item 캐시 적재.
     * 재고는 캐싱하지 않는다 (정합성 우선).
     */
    public RoomTypeRateResult getRoomTypeRates(
            final Long propertyId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);
        propertyReader.getActiveById(propertyId);
        final List<RoomType> roomTypes = roomTypeReader.findActiveByPropertyId(propertyId);
        final List<Long> roomTypeIds = roomTypes.stream().map(RoomType::getId).toList();

        final Map<Long, List<Rate>> ratesByRoomType = roomTypes.stream()
            .collect(toUnmodifiableMap(
                RoomType::getId,
                rt -> rateReader.findByRoomTypeIdAndDateBetween(rt.getId(), startDate, endDate)
            ));
        final Map<Long, List<Inventory>> inventoryByRoomType = inventoryReader.findByRoomTypeIdsAndDateBetween(roomTypeIds, startDate, endDate)
            .stream()
            .collect(groupingBy(Inventory::getRoomTypeId));

        return buildRateResult(propertyId, roomTypes, ratesByRoomType, inventoryByRoomType, startDate, endDate);
    }

    private PropertySearchResult toSearchResult(final Property property, final List<RoomType> roomTypes) {
        return PropertySearchResult.from(property, roomTypes);
    }

    private PropertyDetailResult buildDetailResult(final Property property, final List<RoomType> roomTypes) {
        return PropertyDetailResult.builder()
            .id(property.getId())
            .name(property.getName())
            .type(property.getType().name())
            .description(property.getDescription())
            .address(property.getAddress())
            .region(property.getRegion())
            .latitude(property.getLatitude())
            .longitude(property.getLongitude())
            .checkInTime(property.getCheckInTime())
            .checkOutTime(property.getCheckOutTime())
            .thumbnailUrl(property.getThumbnailUrl())
            .images(toImageEntries(property))
            .roomTypes(toRoomTypeEntries(roomTypes))
            .build();
    }

    private List<PropertyImageEntryResult> toImageEntries(final Property property) {
        return property.getImages().stream()
            .map(PropertyImageEntryResult::from)
            .toList();
    }

    private List<RoomTypeEntryResult> toRoomTypeEntries(final List<RoomType> roomTypes) {
        return roomTypes.stream()
            .map(RoomTypeEntryResult::from)
            .toList();
    }

    private void validateDateRange(final LocalDate startDate, final LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        final long days = dayCountInclusive(startDate, endDate);
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG);
        }
    }

    private RoomTypeRateResult buildRateResult(
            final Long propertyId,
            final List<RoomType> roomTypes,
            final Map<Long, List<Rate>> ratesByRoomType,
            final Map<Long, List<Inventory>> inventoryByRoomType,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        final List<RoomTypeRateEntryResult> entries = roomTypes.stream()
            .map(rt -> buildRoomTypeRateEntry(rt, ratesByRoomType, inventoryByRoomType, startDate, endDate))
            .toList();

        return RoomTypeRateResult.builder()
            .propertyId(propertyId)
            .roomTypes(entries)
            .build();
    }

    private RoomTypeRateEntryResult buildRoomTypeRateEntry(
            final RoomType roomType,
            final Map<Long, List<Rate>> ratesByRoomType,
            final Map<Long, List<Inventory>> inventoryByRoomType,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        final Map<LocalDate, BigDecimal> rateMap = ratesByRoomType
            .getOrDefault(roomType.getId(), List.of())
            .stream()
            .collect(toMap(Rate::getDate, Rate::getPrice));

        final Map<LocalDate, Inventory> inventoryMap = inventoryByRoomType
            .getOrDefault(roomType.getId(), List.of())
            .stream()
            .collect(toMap(Inventory::getDate, i -> i));

        final List<DailyRateResult> dailyRates = startDate.datesUntil(endDate.plusDays(1))
            .map(date -> {
                final BigDecimal price = rateMap.getOrDefault(date, roomType.getBasePrice());
                final Inventory inv = inventoryMap.get(date);
                final boolean available = inv != null && inv.getAvailableCount() > 0;

                return DailyRateResult.builder()
                    .date(date)
                    .price(price)
                    .available(available)
                    .build();
            })
            .toList();

        return RoomTypeRateEntryResult.builder()
            .id(roomType.getId())
            .name(roomType.getName())
            .maxOccupancy(roomType.getMaxOccupancy())
            .rates(dailyRates)
            .build();
    }
}
