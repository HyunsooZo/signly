package com.signly.common.cache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

import com.signly.common.config.WarmupMetrics;

@Slf4j
@Tag(name = "Cache Management", description = "캐시 관리 API (관리자용)")
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;
    private final CacheWarmingService cacheWarmingService;
    private final CacheScheduleService cacheScheduleService;
    private final WarmupMetrics warmupMetrics;

    @Operation(summary = "캐시 정보 조회", description = "모든 캐시의 상태 정보 조회")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        return ResponseEntity.ok(cacheManagementService.getCacheInfo());
    }

    @Operation(summary = "모든 캐시 이름 조회")
    @GetMapping("/names")
    public ResponseEntity<Collection<String>> getAllCacheNames() {
        return ResponseEntity.ok(cacheManagementService.getAllCacheNames());
    }

    @Operation(summary = "특정 캐시 삭제", description = "특정 캐시의 모든 엔트리 삭제")
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<String> evictCache(@PathVariable String cacheName) {
        cacheManagementService.evictCache(cacheName);
        return ResponseEntity.ok("Cache evicted: " + cacheName);
    }

    @Operation(summary = "모든 캐시 삭제", description = "시스템의 모든 캐시 삭제")
    @DeleteMapping("/all")
    public ResponseEntity<String> evictAllCaches() {
        cacheManagementService.evictAllCaches();
        return ResponseEntity.ok("All caches evicted");
    }

    @Operation(summary = "특정 캐시의 특정 키 삭제")
    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<String> evictCacheKey(
            @PathVariable String cacheName,
            @PathVariable String key
    ) {
        cacheManagementService.evictCacheKey(cacheName, key);
        return ResponseEntity.ok("Cache key evicted: " + cacheName + " -> " + key);
    }

    @Operation(summary = "Phase 1 캐시 삭제", description = "변수 정의 및 프리셋 템플릿 캐시 삭제")
    @DeleteMapping("/phase1")
    public ResponseEntity<String> evictPhase1Caches() {
        cacheManagementService.evictPhase1Caches();
        return ResponseEntity.ok("Phase 1 caches evicted");
    }

    @Operation(summary = "Phase 2 캐시 삭제", description = "템플릿 및 사용자 캐시 삭제")
    @DeleteMapping("/phase2")
    public ResponseEntity<String> evictPhase2Caches() {
        cacheManagementService.evictPhase2Caches();
        return ResponseEntity.ok("Phase 2 caches evicted");
    }

    @Operation(summary = "Phase 3 캐시 삭제", description = "대시보드 통계 및 서명 상태 캐시 삭제")
    @DeleteMapping("/phase3")
    public ResponseEntity<String> evictPhase3Caches() {
        cacheManagementService.evictPhase3Caches();
        return ResponseEntity.ok("Phase 3 caches evicted");
    }

    // === Cache Warming Management Endpoints (Phase 4) ===

    @Operation(summary = "캐시 워밍 통계 조회", description = "캐시 워밍 성공/실패 통계 및 메트릭 정보")
    @GetMapping("/warming/stats")
    public ResponseEntity<WarmupMetrics.WarmupStatistics> getWarmingStats() {
        WarmupMetrics.WarmupStatistics stats = warmupMetrics.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "수동 캐시 워밍 실행", description = "변수 정의와 프리셋 템플릿 캐시를 수동으로 워밍")
    @PostMapping("/warming/manual")
    public ResponseEntity<String> manualWarmup() {
        log.info("Manual cache warming requested via API");
        try {
            cacheWarmingService.manualWarmup();
            return ResponseEntity.ok("Cache warming completed successfully");
        } catch (Exception e) {
            log.error("Manual cache warming failed", e);
            return ResponseEntity.internalServerError().body("Cache warming failed: " + e.getMessage());
        }
    }

    @Operation(summary = "변수 정의 캐시 워밍", description = "변수 정의 캐시만 수동으로 워밍")
    @PostMapping("/warming/variables")
    public ResponseEntity<String> warmVariableDefinitions() {
        log.info("Variable definitions warming requested via API");
        try {
            cacheWarmingService.warmVariableDefinitionsOnly();
            return ResponseEntity.ok("Variable definitions warming completed successfully");
        } catch (Exception e) {
            log.error("Variable definitions warming failed", e);
            return ResponseEntity.internalServerError().body("Variable definitions warming failed: " + e.getMessage());
        }
    }

    @Operation(summary = "프리셋 템플릿 캐시 워밍", description = "프리셋 템플릿 캐시만 수동으로 워밍")
    @PostMapping("/warming/presets")
    public ResponseEntity<String> warmTemplatePresets() {
        log.info("Template presets warming requested via API");
        try {
            cacheWarmingService.warmTemplatePresetsOnly();
            return ResponseEntity.ok("Template presets warming completed successfully");
        } catch (Exception e) {
            log.error("Template presets warming failed", e);
            return ResponseEntity.internalServerError().body("Template presets warming failed: " + e.getMessage());
        }
    }

    @Operation(summary = "수동 일일 캐시 갱신", description = "매일 실행되는 변수 정의 캐시 갱신을 수동으로 실행")
    @PostMapping("/scheduling/daily-refresh")
    public ResponseEntity<String> manualDailyRefresh() {
        log.info("Manual daily cache refresh requested via API");
        try {
            cacheScheduleService.manualDailyRefresh();
            return ResponseEntity.ok("Daily cache refresh completed successfully");
        } catch (Exception e) {
            log.error("Manual daily refresh failed", e);
            return ResponseEntity.internalServerError().body("Daily refresh failed: " + e.getMessage());
        }
    }

    @Operation(summary = "수동 주간 캐시 갱신", description = "매주 실행되는 프리셋 템플릿 캐시 갱신을 수동으로 실행")
    @PostMapping("/scheduling/weekly-refresh")
    public ResponseEntity<String> manualWeeklyRefresh() {
        log.info("Manual weekly cache refresh requested via API");
        try {
            cacheScheduleService.manualWeeklyRefresh();
            return ResponseEntity.ok("Weekly cache refresh completed successfully");
        } catch (Exception e) {
            log.error("Manual weekly refresh failed", e);
            return ResponseEntity.internalServerError().body("Weekly refresh failed: " + e.getMessage());
        }
    }
}
