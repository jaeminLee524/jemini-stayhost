package com.jemini.stayhost.common.event;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.jemini.stayhost.property.domain.event.PropertyUpdatedEvent;
import com.jemini.stayhost.property.domain.event.RateUpdatedEvent;
import com.jemini.stayhost.property.domain.event.RoomTypeUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacheEvictListenerTest {

    private CacheEvictListener cacheEvictListener;
    private CaffeineCache searchCache;
    private CaffeineCache propertyCache;
    private CaffeineCache roomTypesCache;
    private CaffeineCache rateCache;

    @BeforeEach
    void setUp() {
        searchCache = new CaffeineCache("search", Caffeine.newBuilder().build());
        propertyCache = new CaffeineCache("property", Caffeine.newBuilder().build());
        roomTypesCache = new CaffeineCache("roomTypes", Caffeine.newBuilder().build());
        rateCache = new CaffeineCache("rate", Caffeine.newBuilder().build());

        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(searchCache, propertyCache, roomTypesCache, rateCache));
        cacheManager.afterPropertiesSet();

        cacheEvictListener = new CacheEvictListener(cacheManager);
    }

    @Test
    @DisplayName("요금 변경 시 해당 roomTypeId의 rate 캐시만 제거한다")
    void 요금_변경_시_해당_roomTypeId의_rate_캐시만_제거한다() {
        rateCache.put("101:2026-04-01:2026-04-03", List.of());
        rateCache.put("101:2026-04-05:2026-04-10", List.of());
        rateCache.put("102:2026-04-01:2026-04-03", List.of());

        cacheEvictListener.onRateUpdated(RateUpdatedEvent.create(101L, List.of(LocalDate.of(2026, 4, 2))));

        assertThat(rateCache.get("101:2026-04-01:2026-04-03")).isNull();
        assertThat(rateCache.get("101:2026-04-05:2026-04-10")).isNull();
        assertThat(rateCache.get("102:2026-04-01:2026-04-03")).isNotNull();
    }

    @Test
    @DisplayName("숙소 정보 변경 시 property 캐시와 search 캐시를 제거한다")
    void 숙소_정보_변경_시_property_캐시와_search_캐시를_제거한다() {
        propertyCache.put(42L, "cached-property");
        propertyCache.put(43L, "other-property");
        searchCache.put("search-key-1", "cached-search");

        cacheEvictListener.onPropertyUpdated(PropertyUpdatedEvent.create(42L));

        assertThat(propertyCache.get(42L)).isNull();
        assertThat(propertyCache.get(43L)).isNotNull();
        assertThat(searchCache.get("search-key-1")).isNull();
    }

    @Test
    @DisplayName("객실 유형 변경 시 roomTypes, property, search 캐시를 모두 제거한다")
    void 객실_유형_변경_시_roomTypes_property_search_캐시를_모두_제거한다() {
        roomTypesCache.put(42L, "cached-roomTypes");
        propertyCache.put(42L, "cached-property");
        searchCache.put("key1", "cached-1");
        searchCache.put("key2", "cached-2");

        cacheEvictListener.onRoomTypeUpdated(RoomTypeUpdatedEvent.create(42L));

        assertThat(roomTypesCache.get(42L)).isNull();
        assertThat(propertyCache.get(42L)).isNull();
        assertThat(searchCache.get("key1")).isNull();
        assertThat(searchCache.get("key2")).isNull();
    }

    @Test
    @DisplayName("요금 변경 시 다른 roomTypeId의 rate 캐시는 영향 없다")
    void 요금_변경_시_다른_roomTypeId의_rate_캐시는_영향_없다() {
        rateCache.put("200:2026-04-01:2026-04-03", List.of());
        rateCache.put("201:2026-04-01:2026-04-03", List.of());

        cacheEvictListener.onRateUpdated(RateUpdatedEvent.create(200L, List.of(LocalDate.of(2026, 4, 1))));

        assertThat(rateCache.get("200:2026-04-01:2026-04-03")).isNull();
        assertThat(rateCache.get("201:2026-04-01:2026-04-03")).isNotNull();
    }
}
