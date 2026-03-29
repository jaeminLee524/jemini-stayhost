package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.Rate;

import java.time.LocalDate;
import java.util.List;

public interface RateReader {

    List<Rate> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}
