package com.jemini.stayhost.search.domain.component;

import com.jemini.stayhost.property.domain.model.Rate;

import java.time.LocalDate;
import java.util.List;

public interface RateReaderV2 {

    List<Rate> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}
