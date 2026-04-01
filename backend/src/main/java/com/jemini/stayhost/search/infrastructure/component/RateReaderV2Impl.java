package com.jemini.stayhost.search.infrastructure.component;

import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.infrastructure.persistence.RateRepository;
import com.jemini.stayhost.search.domain.component.RateReaderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateReaderV2Impl implements RateReaderV2 {

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

    @Cacheable(cacheNames = RATE_CACHE_NAME, key = "#roomTypeId + ':' + #date")
    public Rate findByRoomTypeIdAndDate(final Long roomTypeId, final LocalDate date) {
        return rateRepository.findByRoomTypeIdAndDate(roomTypeId, date)
            .orElse(null);
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
