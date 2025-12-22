package com.signly.common.cache;

import com.signly.common.config.WarmupMetrics;
import com.signly.template.application.VariableCacheRefreshEvent;
import com.signly.template.application.VariableDefinitionService;
import com.signly.template.application.dto.VariableDefinitionDto;
import com.signly.template.application.preset.TemplatePresetService;
import com.signly.template.application.preset.TemplatePresetSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmingService {

    private final VariableDefinitionService variableDefinitionService;
    private final TemplatePresetService templatePresetService;
    private final WarmupMetrics warmupMetrics;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmupCache() {
        log.info("=== Starting cache warmup ===");
        long startTime = System.currentTimeMillis();

        try {
            var variablesWarmup = CompletableFuture.runAsync(this::warmVariableDefinitions);

            var presetsWarmup = CompletableFuture.runAsync(this::warmTemplatePresets);

            CompletableFuture.allOf(variablesWarmup, presetsWarmup).get(30, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Cache warmup completed in {}ms ===", duration);
            warmupMetrics.recordWarmupSuccess(duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== Cache warmup failed after {}ms ===", duration, e);
            warmupMetrics.recordWarmupFailure(duration, e.getMessage());
        }
    }

    private void warmVariableDefinitions() {
        log.info("Warming up variable definitions cache...");
        long startTime = System.currentTimeMillis();

        try {
            var allVariables = variableDefinitionService.getAllActiveVariables();
            log.info("Loaded {} variable definitions", allVariables.size());

            Map<String, List<VariableDefinitionDto>> variablesByCategory = variableDefinitionService.getVariablesByCategory();
            log.info("Loaded {} categories of variables", variablesByCategory.size());

            long duration = System.currentTimeMillis() - startTime;
            log.info("Variable definitions warmup completed in {}ms", duration);

        } catch (Exception e) {
            log.error("Failed to warm up variable definitions cache", e);
            throw new RuntimeException("Variable definitions warmup failed", e);
        }
    }

    private void warmTemplatePresets() {
        log.info("Warming up template presets cache...");
        long startTime = System.currentTimeMillis();

        try {
            var presets = templatePresetService.getSummaries();
            log.info("Loaded {} preset templates", presets.size());

            int presetCount = Math.min(presets.size(), 5);
            for (int i = 0; i < presetCount; i++) {
                String presetId = presets.get(i).getId();
                try {
                    templatePresetService.getPreset(presetId);
                    log.debug("Loaded preset: {}", presetId);
                } catch (Exception e) {
                    log.warn("Failed to load preset {}: {}", presetId, e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Template presets warmup completed in {}ms", duration);

        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            log.warn("Redis serialization error during preset warmup (stale cache): {}", e.getMessage());
            log.info("Skipping preset warmup - cache will be populated on first access");
        } catch (Exception e) {
            log.error("Failed to warm up template presets cache", e);
            throw new RuntimeException("Template presets warmup failed", e);
        }
    }

    public void manualWarmup() {
        log.info("Starting manual cache warmup...");
        warmVariableDefinitions();
        warmTemplatePresets();
        log.info("Manual cache warmup completed");
    }

    public void warmVariableDefinitionsOnly() {
        log.info("Warming up variable definitions only...");
        warmVariableDefinitions();
        log.info("Variable definitions warmup completed");
    }

    public void warmTemplatePresetsOnly() {
        log.info("Warming up template presets only...");
        warmTemplatePresets();
        log.info("Template presets warmup completed");
    }

    @EventListener
    @Async
    public void onVariableCacheRefresh(VariableCacheRefreshEvent event) {
        log.info("Received VariableCacheRefreshEvent, rewarming variable definitions cache...");
        try {
            warmVariableDefinitions();
            log.info("Variable definitions cache rewarmed successfully after event");
        } catch (Exception e) {
            log.warn("Failed to rewarm variable definitions cache after event: {}", e.getMessage());
        }
    }
}