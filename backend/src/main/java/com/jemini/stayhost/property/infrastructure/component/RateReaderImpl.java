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

    @Override
    public List<Rate> findByRoomTypeIdsAndDateBetween(
            final List<Long> roomTypeIds,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        if (roomTypeIds.isEmpty()) {
            return List.of();
        }
        return rateRepository.findByRoomTypeIdInAndDateBetween(roomTypeIds, startDate, endDate);
    }
}
