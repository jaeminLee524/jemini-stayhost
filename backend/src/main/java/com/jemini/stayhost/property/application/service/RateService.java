package com.jemini.stayhost.property.application.service;

import static com.jemini.stayhost.common.util.DateUtil.dateRangeInclusive;
import static com.jemini.stayhost.common.util.DateUtil.dayCountInclusive;
import static java.util.stream.Collectors.toMap;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.RateBulkSetCommand;
import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.event.RateUpdatedEvent;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RateService {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    private final RoomTypeReader roomTypeReader;
    private final PropertyReader propertyReader;
    private final RateReader rateReader;
    private final RateManager rateManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 날짜 범위 요금 일괄 설정. 이미 등록된 날짜는 UPSERT 처리된다.
     */
    @Transactional
    public RateBulkSetResult bulkSet(
        final Long roomTypeId,
        final Long partnerId,
        final RateBulkSetCommand command
    ) {
        validateOwnership(roomTypeId, partnerId);
        validateDateRange(command.startDate(), command.endDate());

        final List<Rate> rates = upsertRates(roomTypeId, command);
        rateManager.saveAll(rates);

        final List<LocalDate> affectedDates = rates.stream().map(Rate::getDate).toList();
        eventPublisher.publishEvent(RateUpdatedEvent.create(roomTypeId, affectedDates));

        return buildBulkSetResult(roomTypeId, rates.size(), command);
    }

    /**
     * 날짜 범위 요금 조회.
     */
    @Transactional(readOnly = true)
    public RateListResult getRates(
        final Long roomTypeId,
        final Long partnerId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        validateOwnership(roomTypeId, partnerId);
        validateDateRange(startDate, endDate);

        final List<Rate> rates = rateReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);

        return RateListResult.of(roomTypeId, rates);
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
        final long days = dayCountInclusive(startDate, endDate);
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG);
        }
    }

    private List<Rate> upsertRates(final Long roomTypeId, final RateBulkSetCommand command) {
        final List<LocalDate> dates = generateDates(command.startDate(), command.endDate(), command.daysOfWeek());
        final Map<LocalDate, Rate> existing = loadExistingRates(roomTypeId, command.startDate(), command.endDate());

        return dates.stream()
            .map(date -> upsertSingle(roomTypeId, date, command.price(), existing))
            .toList();
    }

    private List<LocalDate> generateDates(
        final LocalDate startDate,
        final LocalDate endDate,
        final List<Integer> daysOfWeek
    ) {
        return dateRangeInclusive(startDate, endDate).stream()
            .filter(date -> daysOfWeek == null || daysOfWeek.isEmpty() || daysOfWeek.contains(date.getDayOfWeek().getValue()))
            .toList();
    }

    private Map<LocalDate, Rate> loadExistingRates(
        final Long roomTypeId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        return rateReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate)
            .stream()
            .collect(toMap(Rate::getDate, r -> r));
    }

    private Rate upsertSingle(
        final Long roomTypeId,
        final LocalDate date,
        final BigDecimal price,
        final Map<LocalDate, Rate> existing
    ) {
        final Rate rate = existing.get(date);
        if (rate != null) {
            rate.updatePrice(price);
            return rate;
        }
        return Rate.create(roomTypeId, date, price);
    }

    private RateBulkSetResult buildBulkSetResult(
        final Long roomTypeId,
        final int appliedDates,
        final RateBulkSetCommand command
    ) {
        return RateBulkSetResult.builder()
            .roomTypeId(roomTypeId)
            .appliedDates(appliedDates)
            .startDate(command.startDate())
            .endDate(command.endDate())
            .price(command.price())
            .build();
    }
}
