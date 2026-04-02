package com.jemini.stayhost.search.application.service;

import static com.jemini.stayhost.common.util.DateUtil.dayCountInclusive;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.search.application.dto.DailyRateResult;
import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateEntryResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    private final PropertyReader propertyReader;
    private final RoomTypeReader roomTypeReader;
    private final RateReader rateReader;
    private final InventoryReader inventoryReader;

    /**
     * 숙소 검색. ACTIVE 상태의 숙소만 반환한다.
     * <p> N+1 방지: 검색 결과의 모든 숙소 객실을 한 번의 IN 쿼리로 일괄 조회한다.
     * <p> 동일 검색 조건은 1분간 캐시. 숙소/객실 변경 이벤트 시 전체 무효화.
     */
    @Cacheable(value = "search", key = "{#region, #keyword, #pageable.pageNumber, #pageable.pageSize}")
    @Transactional(readOnly = true)
    public PageResult<PropertySearchResult> searchProperties(
        final String region,
        final String keyword,
        final Pageable pageable
    ) {
        final Page<Property> properties = propertyReader.searchActive(region, keyword, pageable);
        final Map<Long, List<RoomType>> roomTypesByProperty = loadRoomTypesByProperty(properties);

        final List<PropertySearchResult> results = properties.getContent().stream()
            .map(property -> PropertySearchResult.from(property, roomTypesByProperty.getOrDefault(property.getId(), List.of())))
            .toList();

        return PageResult.from(new PageImpl<>(results, pageable, properties.getTotalElements()));
    }

    /**
     * 숙소 상세 조회. 객실 유형 목록 포함.
     */
    @Transactional(readOnly = true)
    public PropertyDetailResult getPropertyDetail(final Long propertyId) {
        final Property property = propertyReader.getActiveById(propertyId);
        final List<RoomType> roomTypes = roomTypeReader.findActiveByPropertyId(propertyId);

        return PropertyDetailResult.from(property, roomTypes);
    }

    /**
     * 숙소의 객실별 날짜 범위 요금 조회.
     */
    @Transactional(readOnly = true)
    public RoomTypeRateResult getRoomTypeRates(
        final Long propertyId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);
        propertyReader.getActiveById(propertyId);

        final List<RoomType> roomTypes = roomTypeReader.findActiveByPropertyId(propertyId);
        final List<Long> roomTypeIds = roomTypes.stream().map(RoomType::getId).toList();
        final Map<Long, List<Rate>> ratesByRoomType = loadRatesByRoomType(roomTypes, startDate, endDate);
        final Map<Long, List<Inventory>> inventoryByRoomType = loadInventoryByRoomType(roomTypeIds, startDate, endDate);

        return buildRateResult(propertyId, roomTypes, ratesByRoomType, inventoryByRoomType, startDate, endDate);
    }

    private Map<Long, List<RoomType>> loadRoomTypesByProperty(final Page<Property> properties) {
        final List<Long> propertyIds = properties.getContent().stream().map(Property::getId).toList();

        return roomTypeReader.findActiveByPropertyIds(propertyIds).stream()
            .collect(groupingBy(RoomType::getPropertyId));
    }

    private Map<Long, List<Rate>> loadRatesByRoomType(
        final List<RoomType> roomTypes,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        final List<Long> roomTypeIds = roomTypes.stream().map(RoomType::getId).toList();

        return rateReader.findByRoomTypeIdsAndDateBetween(roomTypeIds, startDate, endDate).stream()
            .collect(groupingBy(Rate::getRoomTypeId));
    }

    private Map<Long, List<Inventory>> loadInventoryByRoomType(
        final List<Long> roomTypeIds,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        return inventoryReader.findByRoomTypeIdsAndDateBetween(roomTypeIds, startDate, endDate).stream()
            .collect(groupingBy(Inventory::getRoomTypeId));
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
        final Map<LocalDate, BigDecimal> rateMap = ratesByRoomType.getOrDefault(roomType.getId(), List.of()).stream()
            .collect(toMap(Rate::getDate, Rate::getPrice));
        final Map<LocalDate, Inventory> inventoryMap = inventoryByRoomType.getOrDefault(roomType.getId(), List.of()).stream()
            .collect(toMap(Inventory::getDate, i -> i));

        final List<DailyRateResult> dailyRates = startDate.datesUntil(endDate.plusDays(1))
            .map(date -> buildDailyRate(roomType, date, rateMap, inventoryMap))
            .toList();

        return RoomTypeRateEntryResult.builder()
            .id(roomType.getId())
            .name(roomType.getName())
            .maxOccupancy(roomType.getMaxOccupancy())
            .rates(dailyRates)
            .build();
    }

    private DailyRateResult buildDailyRate(
        final RoomType roomType,
        final LocalDate date,
        final Map<LocalDate, BigDecimal> rateMap,
        final Map<LocalDate, Inventory> inventoryMap
    ) {
        final BigDecimal price = rateMap.getOrDefault(date, roomType.getBasePrice());
        final Inventory inv = inventoryMap.get(date);
        final boolean available = inv != null && inv.getAvailableCount() > 0;

        return DailyRateResult.create(date, price, available);
    }
}
