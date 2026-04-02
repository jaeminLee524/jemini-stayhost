package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.infrastructure.persistence.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateReaderImpl implements RateReader {

    private static final String RATE_CACHE_NAME = "rate";

    private final RateRepository rateRepository;
    private final CacheManager cacheManager;

    @Override
    public List<Rate> findByRoomTypeIdAndDateBetween(
        final Long roomTypeId,
        final LocalDate startDate,
        final LocalDate endDate
    ) {
        final List<Rate> rates = rateRepository.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);
        warmRatesIntoCache(rates);
        return rates;
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
        final List<Rate> rates = rateRepository.findByRoomTypeIdInAndDateBetween(roomTypeIds, startDate, endDate);
        warmRatesIntoCache(rates);
        return rates;
    }

    private void warmRatesIntoCache(final List<Rate> rates) {
        final Cache cache = cacheManager.getCache(RATE_CACHE_NAME);
        if (cache != null) {
            rates.forEach(rate -> {
                final String key = rate.getRoomTypeId() + ":" + rate.getDate();
                cache.put(key, rate);
            });
        }
    }
}
