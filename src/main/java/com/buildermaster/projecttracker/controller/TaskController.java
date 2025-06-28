package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks")
public class TaskController {

    private final TaskService taskService;

    // ===== CRUD OPERATIONS =====

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> createTask(@Valid @RequestBody CreateTaskRequestDTO request) {
        TaskResponseDTO task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(task));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> getTaskById(@PathVariable UUID taskId) {
        TaskResponseDTO task = taskService.getTaskById(taskId);
        return task != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Task retrieved successfully", task))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Task not found", 404));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update task by ID")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> updateTask(@PathVariable UUID taskId,
                                                                      @Valid @RequestBody UpdateTaskRequestDTO request) {
        TaskResponseDTO updatedTask = taskService.updateTask(taskId, request);
        return updatedTask != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Task updated successfully", updatedTask))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Task not found", 404));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete task by ID")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTask(@PathVariable UUID taskId) {
        boolean deleted = taskService.deleteTask(taskId);
        return deleted
                ? ResponseEntity.ok(ApiResponseDTO.success("Task deleted successfully", null))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Task not found", 404));
    }

    // ===== LISTING TASKS =====

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all tasks with optional pagination")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getAllTasks(@ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getAllTasks(pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found", page));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks by project ID")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByProject(@PathVariable UUID projectId,
                                                                                   @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getTasksByProjectId(projectId, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Project tasks retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found for this project", page));
    }

    @GetMapping("/developer/{developerId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    @Operation(summary = "Get tasks by developer ID")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByDeveloper(@PathVariable UUID developerId,
                                                                                     @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getTasksByDeveloperId(developerId, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer tasks retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found for this developer", page));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks by status")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getTasksByStatus(@PathVariable ETaskStatus status,
                                                                                  @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getTasksByStatus(status, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks by status retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found with this status", page));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get overdue tasks")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getOverdueTasks(@ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getOverdueTasks(pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Overdue tasks retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No overdue tasks found", page));
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get unassigned tasks")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getUnassignedTasks(@ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getUnassignedTasks(pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Unassigned tasks retrieved successfully", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No unassigned tasks found", page));
    }

    // ===== SEARCH AND FILTER =====

    @GetMapping("/search/title")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Search tasks by title")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> searchByTitle(@RequestParam String title,
                                                                               @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.searchTasksByTitle(title, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks found by title", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found matching title", page));
    }

    @GetMapping("/search/description")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Search tasks by description")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> searchByDescription(@RequestParam String description,
                                                                                     @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.searchTasksByDescription(description, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks found by description", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found matching description", page));
    }

    @GetMapping("/due-range")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks by due date range")
    public ResponseEntity<ApiResponseDTO<Page<TaskResponseDTO>>> getByDueDateRange(@RequestParam LocalDate start,
                                                                                   @RequestParam LocalDate end,
                                                                                   @ParameterObject Pageable pageable) {
        Page<TaskResponseDTO> page = taskService.getTasksByDueDateRange(start, end, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks found in date range", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks found in date range", page));
    }

    // ===== ASSIGNMENT OPERATIONS =====

    @PatchMapping("/{taskId}/assign/{developerId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Assign a task to a developer")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> assignTask(@PathVariable UUID taskId,
                                                                      @PathVariable UUID developerId) {
        TaskResponseDTO task = taskService.assignTaskToDeveloper(taskId, developerId);
        return task != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Task assigned successfully", task))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Task or developer not found", 404));
    }

    @PatchMapping("/{taskId}/unassign")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Unassign a task from a developer")
    public ResponseEntity<ApiResponseDTO<TaskResponseDTO>> unassignTask(@PathVariable UUID taskId) {
        TaskResponseDTO task = taskService.unassignTask(taskId);
        return task != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Task unassigned successfully", task))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Task not found", 404));
    }

    // ===== QUICK FILTERS =====

    @GetMapping("/due-today")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks due today")
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getTasksDueToday() {
        List<TaskResponseDTO> tasks = taskService.getTasksDueToday();
        return !tasks.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks due today retrieved", tasks))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks due today", tasks));
    }

    @GetMapping("/due-this-week")
    @Operation(summary = "Get tasks due this week")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getTasksDueThisWeek() {
        List<TaskResponseDTO> tasks = taskService.getTasksDueThisWeek();
        return !tasks.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Tasks due this week retrieved", tasks))
                : ResponseEntity.ok(ApiResponseDTO.success("No tasks due this week", tasks));
    }

    @GetMapping("/created-since")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks created since a specific date")
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getRecentlyCreated(@RequestParam LocalDateTime since) {
        List<TaskResponseDTO> tasks = taskService.getRecentlyCreatedTasks(since);
        return !tasks.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Recently created tasks retrieved", tasks))
                : ResponseEntity.ok(ApiResponseDTO.success("No recently created tasks found", tasks));
    }

    @GetMapping("/updated-since")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get tasks updated since a specific date")
    public ResponseEntity<ApiResponseDTO<List<TaskResponseDTO>>> getRecentlyUpdated(@RequestParam LocalDateTime since) {
        List<TaskResponseDTO> tasks = taskService.getRecentlyUpdatedTasks(since);
        return !tasks.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Recently updated tasks retrieved", tasks))
                : ResponseEntity.ok(ApiResponseDTO.success("No recently updated tasks found", tasks));
    }

    // ===== STATISTICS =====

    @GetMapping("/stats")
    @Operation(summary = "Get overall task statistics")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getTaskStatistics() {
        TaskStatsDTO stats = taskService.getTaskStatistics();
        return stats != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Task statistics retrieved", stats))
                : ResponseEntity.ok(ApiResponseDTO.success("No statistics available", stats));
    }

    @GetMapping("/stats/project/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get task statistics for a specific project")
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getProjectTaskStatistics(@PathVariable UUID projectId) {
        TaskStatsDTO stats = taskService.getTaskStatisticsByProject(projectId);
        return stats != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Project statistics retrieved", stats))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Project not found", 404));
    }

    @GetMapping("/stats/developer/{developerId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    @Operation(summary = "Get task statistics for a specific developer")
    public ResponseEntity<ApiResponseDTO<TaskStatsDTO>> getDeveloperTaskStatistics(@PathVariable UUID developerId) {
        TaskStatsDTO stats = taskService.getTaskStatisticsByDeveloper(developerId);
        return stats != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer statistics retrieved", stats))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    @GetMapping("/counts/status")
    @Operation(summary = "Get task counts grouped by status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Map<ETaskStatus, Long>>> getCountsByStatus() {
        Map<ETaskStatus, Long> counts = taskService.getTaskCountsByStatus();
        return !counts.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Status counts retrieved", counts))
                : ResponseEntity.ok(ApiResponseDTO.success("No task counts available", counts));
    }

    @GetMapping("/counts/project/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get task count for a specific project")
    public ResponseEntity<ApiResponseDTO<Long>> getCountByProject(@PathVariable UUID projectId) {
        Long count = taskService.getTaskCountByProject(projectId);
        return count != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Project task count retrieved", count))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Project not found", 404));
    }

    @GetMapping("/counts/developer/{developerId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    @Operation(summary = "Get task count for a specific developer")
    public ResponseEntity<ApiResponseDTO<Long>> getCountByDeveloper(@PathVariable UUID developerId) {
        Long count = taskService.getTaskCountByDeveloper(developerId);
        return count != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer task count retrieved", count))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    // ===== SUMMARIES =====

    @GetMapping("/summaries/project/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get task summaries by project")
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getProjectSummaries(@PathVariable UUID projectId,
                                                                                    @ParameterObject Pageable pageable) {
        Page<TaskSummaryDTO> page = taskService.getTaskSummariesByProject(projectId, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Project summaries retrieved", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No project summaries found", page));
    }

    @GetMapping("/summaries/developer/{developerId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    @Operation(summary = "Get task summaries by developer")
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getDeveloperSummaries(@PathVariable UUID developerId,
                                                                                      @ParameterObject Pageable pageable) {
        Page<TaskSummaryDTO> page = taskService.getTaskSummariesByDeveloper(developerId, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer summaries retrieved", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No developer summaries found", page));
    }

    @GetMapping("/summaries/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get task summaries by status")
    public ResponseEntity<ApiResponseDTO<Page<TaskSummaryDTO>>> getStatusSummaries(@PathVariable ETaskStatus status,
                                                                                   @ParameterObject Pageable pageable) {
        Page<TaskSummaryDTO> page = taskService.getTaskSummariesByStatus(status, pageable);
        return page.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Status summaries retrieved", page))
                : ResponseEntity.ok(ApiResponseDTO.success("No status summaries found", page));
    }
}