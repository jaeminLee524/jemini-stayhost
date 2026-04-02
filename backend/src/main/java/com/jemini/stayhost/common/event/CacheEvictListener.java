package com.jemini.stayhost.common.event;

import com.jemini.stayhost.property.domain.event.PropertyUpdatedEvent;
import com.jemini.stayhost.property.domain.event.RateUpdatedEvent;
import com.jemini.stayhost.property.domain.event.RoomTypeUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPropertyUpdated(final PropertyUpdatedEvent event) {
        evict("property", event.propertyId());
        clearCache("search");
        log.debug("캐시 무효화: property:{}, search 전체", event.propertyId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomTypeUpdated(final RoomTypeUpdatedEvent event) {
        evict("roomTypes", event.propertyId());
        evict("property", event.propertyId());
        clearCache("search");
        log.debug("캐시 무효화: roomTypes:{}, property:{}, search 전체", event.propertyId(), event.propertyId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRateUpdated(final RateUpdatedEvent event) {
        evictByPrefix("rate", event.roomTypeId() + ":");
        log.debug("캐시 무효화: rate roomTypeId={}", event.roomTypeId());
    }

    private void evict(final String cacheName, final Object key) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private void evictByPrefix(final String cacheName, final String prefix) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof CaffeineCache caffeineCache) {
            caffeineCache.getNativeCache().asMap().keySet().removeIf(
                key -> key.toString().startsWith(prefix)
            );
        }
    }

    private void clearCache(final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
