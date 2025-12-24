package com.signly.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer implements ApplicationRunner {

    private final CacheManager cacheManager;

    @Override
    public void run(ApplicationArguments args) {
        log.info("🔄 Initializing caches on application startup...");

        // 모든 캐시 초기화 (역직렬화 오류 방지)
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("✅ Cleared '{}' cache", cacheName);
            }
        });

        log.info("✨ Cache initialization completed");
    }
}
