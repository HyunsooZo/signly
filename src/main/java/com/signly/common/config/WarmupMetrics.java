package com.signly.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarmupMetrics {

    private final MeterRegistry meterRegistry;

    private final AtomicInteger warmupSuccessCount = new AtomicInteger(0);
    private final AtomicInteger warmupFailureCount = new AtomicInteger(0);
    private Timer warmupTimer;

    public void recordWarmupSuccess(long durationMs) {
        warmupSuccessCount.incrementAndGet();
        getWarmupTimer().record(Duration.ofMillis(durationMs));

        log.info("Cache warmup success #{} ({}ms)", warmupSuccessCount.get(), durationMs);
    }

    public void recordWarmupFailure(
            long durationMs,
            String errorMessage
    ) {
        warmupFailureCount.incrementAndGet();
        getWarmupTimer().record(Duration.ofMillis(durationMs));

        log.error("Cache warmup failure #{} ({}ms): {}", warmupFailureCount.get(), durationMs, errorMessage);
    }

    private Timer getWarmupTimer() {
        if (warmupTimer == null) {
            warmupTimer = Timer.builder("cache.warmup.time")
                    .description("Cache warmup execution time")
                    .register(meterRegistry);
        }
        return warmupTimer;
    }

    public WarmupStatistics getStatistics() {
        return new WarmupStatistics(
                warmupSuccessCount.get(),
                warmupFailureCount.get()
        );
    }

    public record WarmupStatistics(
            int successCount,
            int failureCount
    ) {
        public int totalCount() {
            return successCount + failureCount;
        }

        public double successRate() {
            if (totalCount() == 0) return 0.0;
            return (double) successCount / totalCount() * 100;
        }
    }
}