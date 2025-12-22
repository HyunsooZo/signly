package com.signly.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collection;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CacheMetricsConfig {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void bindCacheMetrics() {
        var cacheNames = cacheManager.getCacheNames();

        for (var cacheName : cacheNames) {
            var cache = cacheManager.getCache(cacheName);
            if (cache instanceof RedisCache redisCache) {
                try {
                    meterRegistry.gauge("cache.size", cache, c -> {
                        return 0;
                    });

                    log.info("Cache metrics bound for: {}", cacheName);
                } catch (Exception e) {
                    log.warn("Failed to bind metrics for cache: {}", cacheName, e);
                }
            }
        }

        log.info("Cache metrics binding completed for {} caches", cacheNames.size());
    }

    @Scheduled(fixedRate = 300000)
    public void logCacheStatistics() {
        log.info("=== Cache Statistics Report ===");

        var cacheNames = cacheManager.getCacheNames();
        for (var cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Cache [{}] is active", cacheName);
            }
        }

        log.info("Total active caches: {}", cacheNames.size());
        log.info("===============================");
    }
}
