package com.jemini.stayhost.property.application.service;

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
  public RateBulkSetResult bulkSet(final Long roomTypeId, final Long partnerId, final RateBulkSetCommand command) {
    final RoomType roomType = roomTypeReader.getById(roomTypeId);
    validateRoomTypeOwner(roomType, partnerId);

    final List<LocalDate> dates = generateDates(command.startDate(), command.endDate(), command.daysOfWeek());
    final Map<LocalDate, Rate> existing = rateReader
        .findByRoomTypeIdAndDateBetween(roomTypeId, command.startDate(), command.endDate())
        .stream().collect(Collectors.toMap(Rate::getDate, r -> r));

    final List<Rate> rates = dates.stream()
        .map(date -> {
          final Rate rate = existing.get(date);
          if (rate != null) {
            rate.updatePrice(command.price());
            return rate;
          }
          return Rate.create(roomTypeId, date, command.price());
        })
        .toList();

    rateManager.saveAll(rates);

    return RateBulkSetResult.builder()
        .roomTypeId(roomTypeId)
        .appliedDates(rates.size())
        .startDate(command.startDate())
        .endDate(command.endDate())
        .price(command.price())
        .build();
  }

  /**
   * 날짜 범위 요금 조회.
   */
  @Transactional(readOnly = true)
  public RateListResult getRates(final Long roomTypeId, final Long partnerId, final LocalDate startDate, final LocalDate endDate) {
    final RoomType roomType = roomTypeReader.getById(roomTypeId);
    validateRoomTypeOwner(roomType, partnerId);

    final List<Rate> rates = rateReader.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);

    return RateListResult.of(roomTypeId, rates);
  }

  private void validateRoomTypeOwner(final RoomType roomType, final Long partnerId) {
    final Property property = propertyReader.getById(roomType.getPropertyId());
    property.validateOwner(partnerId);
  }

  private List<LocalDate> generateDates(final LocalDate startDate, final LocalDate endDate, final List<Integer> daysOfWeek) {
    return startDate.datesUntil(endDate.plusDays(1))
        .filter(date -> daysOfWeek == null || daysOfWeek.isEmpty() || daysOfWeek.contains(date.getDayOfWeek().getValue()))
        .toList();
  }
}
