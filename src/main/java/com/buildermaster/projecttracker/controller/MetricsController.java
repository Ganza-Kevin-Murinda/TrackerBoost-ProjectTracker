package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.repository.TaskRepository;
import com.buildermaster.projecttracker.service.impl.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/metrics")
@PreAuthorize("hasRole('MANAGER')")  // Assuming only managers should access metrics
@Slf4j
public class MetricsController {

    private final MetricsService metricsService;
    private final TaskRepository taskRepository;
    private final MeterRegistry meterRegistry;

    public MetricsController(MetricsService metricsService,
                             TaskRepository taskRepository,
                             MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.taskRepository = taskRepository;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/tasks/summary")
    public ResponseEntity<Map<String, Object>> getTaskMetricsSummary() {
        log.debug("Fetching task metrics summary");

        Map<String, Object> metrics = new HashMap<>();

        // Basic task counts
        metrics.put("totalTasks", taskRepository.count());
        metrics.put("overdueTasks", taskRepository.findOverdueTasks().size());
        metrics.put("unassignedTasks", taskRepository.findUnassignedTasks().size());

        // Operation counts
        Map<String, Number> operationMetrics = metricsService.getAllMetrics();
        metrics.put("operations", operationMetrics);

        // Performance metrics
        Timer taskTimer = meterRegistry.timer("app.tasks.processing.time");
        metrics.put("performanceMetrics", Map.of(
                "meanProcessingTimeMs", taskTimer.mean(TimeUnit.MILLISECONDS),
                "maxProcessingTimeMs", taskTimer.max(TimeUnit.MILLISECONDS),
                "totalProcessingTimeMs", taskTimer.totalTime(TimeUnit.MILLISECONDS)
        ));

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/tasks/operations")
    public ResponseEntity<Map<String, Number>> getTaskOperationMetrics() {
        log.debug("Fetching task operation metrics");
        return ResponseEntity.ok(metricsService.getAllMetrics());
    }

    @GetMapping("/tasks/performance")
    public ResponseEntity<Map<String, Double>> getTaskPerformanceMetrics() {
        log.debug("Fetching task performance metrics");

        Timer taskTimer = meterRegistry.timer("app.tasks.processing.time");
        Map<String, Double> performanceMetrics = new HashMap<>();

        performanceMetrics.put("meanProcessingTimeMs", taskTimer.mean(TimeUnit.MILLISECONDS));
        performanceMetrics.put("maxProcessingTimeMs", taskTimer.max(TimeUnit.MILLISECONDS));
        performanceMetrics.put("totalProcessingTimeMs", taskTimer.totalTime(TimeUnit.MILLISECONDS));
        performanceMetrics.put("count", (double) taskTimer.count());

        return ResponseEntity.ok(performanceMetrics);
    }

    @GetMapping("/tasks/status-distribution")
    public ResponseEntity<Map<String, Object>> getTaskStatusDistribution() {
        log.debug("Fetching task status distribution metrics");

        Map<String, Object> distribution = new HashMap<>();
        long totalTasks = taskRepository.count();

        // Get counts by status
        List<Object[]> statusCounts = taskRepository.countTasksByStatus();
        Map<String, Double> percentages = new HashMap<>();

        for (Object[] result : statusCounts) {
            ETaskStatus status = (ETaskStatus) result[0];
            Long count = (Long) result[1];
            double percentage = totalTasks > 0 ? (count * 100.0) / totalTasks : 0;
            percentages.put(status.name(), Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places
        }

        distribution.put("totalTasks", totalTasks);
        distribution.put("statusPercentages", percentages);

        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> getMetricsHealthCheck() {
        log.debug("Performing metrics health check");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());

        try {
            // Try to record a metric to verify the metrics system is working
            meterRegistry.counter("health.check").increment();
            health.put("metricsSystem", "operational");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("metricsSystem", "failed");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }

        return ResponseEntity.ok(health);
    }
}
