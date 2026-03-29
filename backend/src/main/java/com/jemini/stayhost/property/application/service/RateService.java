package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.RateBulkSetCommand;
import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RoomTypeReader roomTypeReader;
    private final PropertyReader propertyReader;
    private final RateReader rateReader;
    private final RateManager rateManager;

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
    }

    private List<Rate> upsertRates(final Long roomTypeId, final RateBulkSetCommand command) {
        final List<LocalDate> dates = generateDates(command.startDate(), command.endDate(), command.daysOfWeek());
        final Map<LocalDate, Rate> existing = loadExistingRates(roomTypeId, command.startDate(), command.endDate());

        return dates.stream()
                .map(date -> upsertSingle(roomTypeId, date, command.price(), existing))
                .toList();
    }

    private Map<LocalDate, Rate> loadExistingRates(
            final Long roomTypeId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        return rateReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(Rate::getDate, r -> r));
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

    private List<LocalDate> generateDates(
            final LocalDate startDate,
            final LocalDate endDate,
            final List<Integer> daysOfWeek
    ) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> daysOfWeek == null || daysOfWeek.isEmpty() || daysOfWeek.contains(date.getDayOfWeek().getValue()))
                .toList();
    }
}
