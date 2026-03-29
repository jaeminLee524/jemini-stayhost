package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.infrastructure.persistence.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateReaderImpl implements RateReader {

    private final RateRepository rateRepository;

    @Override
    public List<Rate> findByRoomTypeIdAndDateBetween(
            final Long roomTypeId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        return rateRepository.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);
    }
}
