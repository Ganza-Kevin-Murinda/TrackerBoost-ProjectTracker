package com.buildermaster.projecttracker.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize all counters
        Counter.builder("app.tasks.created")
                .description("Number of tasks created")
                .register(meterRegistry);

        Counter.builder("app.tasks.updated")
                .description("Number of tasks updated")
                .register(meterRegistry);

        Counter.builder("app.tasks.deleted")
                .description("Number of tasks deleted")
                .register(meterRegistry);

        Counter.builder("app.tasks.retrieved")
                .description("Number of tasks retrieved")
                .register(meterRegistry);

        Counter.builder("app.tasks.assigned")
                .description("Number of tasks assigned")
                .register(meterRegistry);

        Counter.builder("app.tasks.unassigned")
                .description("Number of tasks unassigned")
                .register(meterRegistry);

        Timer.builder("app.tasks.processing.time")
                .description("Task processing time")
                .register(meterRegistry);
    }

    public void incrementTasksCreated() {
        meterRegistry.counter("app.tasks.created").increment();
    }

    public void incrementTasksUpdated() {
        meterRegistry.counter("app.tasks.updated").increment();
    }

    public void incrementTasksDeleted() {
        meterRegistry.counter("app.tasks.deleted").increment();
    }

    public void incrementTasksRetrieved() {
        meterRegistry.counter("app.tasks.retrieved").increment();
    }

    public void incrementTasksAssigned() {
        meterRegistry.counter("app.tasks.assigned").increment();
    }

    public void incrementTasksUnassigned() {
        meterRegistry.counter("app.tasks.unassigned").increment();
    }

    public void recordTaskProcessingTime(long timeInMs) {
        meterRegistry.timer("app.tasks.processing.time")
                .record(timeInMs, TimeUnit.MILLISECONDS);
    }

    public void recordTaskStatisticsGenerated() {
        meterRegistry.counter("app.tasks.statistics.generated").increment();
    }

    // Method to get all metrics
    public Map<String, Number> getAllMetrics() {
        Map<String, Number> metrics = new HashMap<>();

        metrics.put("tasksCreated", meterRegistry.counter("app.tasks.created").count());
        metrics.put("tasksUpdated", meterRegistry.counter("app.tasks.updated").count());
        metrics.put("tasksDeleted", meterRegistry.counter("app.tasks.deleted").count());
        metrics.put("tasksRetrieved", meterRegistry.counter("app.tasks.retrieved").count());
        metrics.put("tasksAssigned", meterRegistry.counter("app.tasks.assigned").count());
        metrics.put("tasksUnassigned", meterRegistry.counter("app.tasks.unassigned").count());

        Timer timer = meterRegistry.timer("app.tasks.processing.time");
        metrics.put("averageProcessingTimeMs", timer.mean(TimeUnit.MILLISECONDS));
        metrics.put("maxProcessingTimeMs", timer.max(TimeUnit.MILLISECONDS));

        return metrics;
    }
}