package com.jemini.stayhost.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        final SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            buildCache("property", 5_000, 10, TimeUnit.MINUTES),
            buildCache("roomTypes", 5_000, 10, TimeUnit.MINUTES),
            buildCache("rate", 30_000, 3, TimeUnit.MINUTES)
        ));
        return manager;
    }

    private CaffeineCache buildCache(
            final String name,
            final int maxSize,
            final long duration,
            final TimeUnit unit
    ) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration, unit)
                .recordStats()
                .build());
    }
}
