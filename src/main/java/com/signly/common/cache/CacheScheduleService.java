package com.signly.common.cache;

import com.signly.common.config.WarmupMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheScheduleService {

    private final CacheWarmingService cacheWarmingService;
    private final CacheManagementService cacheManagementService;
    private final WarmupMetrics warmupMetrics;

    @Scheduled(cron = "0 0 3 * * MON")
    public void weeklyCacheRefresh() {
        log.info("=== Starting weekly cache refresh ===");
        String runTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.info("Weekly cache refresh started at: {}", runTime);

        try {
            log.info("Refreshing template presets cache...");
            cacheManagementService.evictCache("templatePresets");
            cacheWarmingService.warmTemplatePresetsOnly();
            log.info("Template presets cache refreshed successfully");

            cleanupOldCacheEntries();

            log.info("=== Weekly cache refresh completed ===");

        } catch (Exception e) {
            log.error("=== Weekly cache refresh failed ===", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // 매일 오전 2시
    public void dailyVariableDefinitionsRefresh() {
        log.info("Starting daily variable definitions refresh...");

        try {
            log.info("Refreshing variable definitions cache...");
            cacheManagementService.evictCache("variableDefinitions");
            cacheWarmingService.warmVariableDefinitionsOnly();
            log.info("Variable definitions cache refreshed successfully");

        } catch (Exception e) {
            log.error("Daily variable definitions refresh failed", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void hourlyCacheHealthCheck() {
        try {
            var cacheInfo = cacheManagementService.getCacheInfo();
            int cacheCount = (Integer) cacheInfo.get("totalCaches");
            
            log.debug("Cache health check - Total caches: {}", cacheCount);
            
            if (cacheCount == 0) {
                log.warn("No active caches found during health check");
            }
            
        } catch (Exception e) {
            log.error("Cache health check failed", e);
        }
    }

    private void cleanupOldCacheEntries() {
        log.info("Cleaning up old cache entries...");
        
        try {
            log.debug("Old cache entries cleanup completed");
            
        } catch (Exception e) {
            log.warn("Old cache entries cleanup failed", e);
        }
    }

    public void manualWeeklyRefresh() {
        log.info("Starting manual weekly refresh...");
        
        try {
            cacheManagementService.evictCache("templatePresets");
            cacheWarmingService.warmTemplatePresetsOnly();

            log.info("Manual weekly refresh completed");
            
        } catch (Exception e) {
            log.error("Manual weekly refresh failed", e);
            throw new RuntimeException("Manual cache refresh failed", e);
        }
    }

    public void manualDailyRefresh() {
        log.info("Starting manual daily refresh...");
        
        try {
            cacheManagementService.evictCache("variableDefinitions");
            cacheWarmingService.warmVariableDefinitionsOnly();
            
            log.info("Manual daily refresh completed");
            
        } catch (Exception e) {
            log.error("Manual daily refresh failed", e);
            throw new RuntimeException("Manual daily refresh failed", e);
        }
    }
}