package com.signly.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagementService {

    private final CacheManager cacheManager;

    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache evicted: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    public void evictAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        cacheNames.forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        log.info("All caches evicted: {}", cacheNames);
    }

    public void evictCacheKey(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Cache key evicted: {} -> {}", cacheName, key);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    public Collection<String> getAllCacheNames() {
        return cacheManager.getCacheNames();
    }

    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        Collection<String> cacheNames = cacheManager.getCacheNames();
        
        info.put("totalCaches", cacheNames.size());
        info.put("cacheNames", cacheNames);
        info.put("cacheManager", cacheManager.getClass().getSimpleName());
        
        return info;
    }

    public void evictPhase1Caches() {
        evictCache("variableDefinitions");
        evictCache("templatePresets");
        log.info("Phase 1 caches evicted (variableDefinitions, templatePresets)");
    }

    public void evictPhase2Caches() {
        evictCache("templates");
        evictCache("users");
        evictCache("userDetails");
        log.info("Phase 2 caches evicted (templates, users, userDetails)");
    }

    public void evictPhase3Caches() {
        evictCache("dashboardStats");
        evictCache("signatureStatus");
        evictCache("contractsByToken");
        log.info("Phase 3 caches evicted (dashboardStats, signatureStatus, contractsByToken)");
    }
}
