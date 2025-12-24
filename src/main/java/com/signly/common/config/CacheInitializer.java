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

        Cache userDetailsCache = cacheManager.getCache("userDetails");
        if (userDetailsCache != null) {
            userDetailsCache.clear();
            log.info("✅ Cleared 'userDetails' cache to prevent deserialization errors");
        } else {
            log.warn("⚠️ 'userDetails' cache not found, skipping clear");
        }

        log.info("✨ Cache initialization completed");
    }
}
