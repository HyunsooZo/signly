package com.signly.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionService {

    @CacheEvict(value = "dashboardStats", key = "#userId + ':templates'")
    public void evictTemplateStats(String userId) {
        log.info("Evicted template stats cache for user: {}", userId);
    }

    @CacheEvict(value = "dashboardStats", key = "#userId + ':contracts'")
    public void evictContractStats(String userId) {
        log.info("Evicted contract stats cache for user: {}", userId);
    }

    @CacheEvict(value = "dashboardStats", allEntries = true)
    public void evictAllDashboardStats() {
        log.info("Evicted all dashboard stats cache");
    }

    @CacheEvict(value = "contractsByToken", allEntries = true)
    public void evictContractTokenCaches() {
        log.info("Evicted all contract token caches");
    }

    public void evictUserDashboardStats(String userId) {
        evictTemplateStats(userId);
        evictContractStats(userId);
        log.info("Evicted dashboard stats cache for user: {}", userId);
    }
}