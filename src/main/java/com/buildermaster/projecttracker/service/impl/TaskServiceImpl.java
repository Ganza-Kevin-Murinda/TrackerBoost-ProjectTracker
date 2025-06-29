package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.dto.request.CreateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.exception.ResourceNotFoundException;
import com.buildermaster.projecttracker.exception.ValidationException;
import com.buildermaster.projecttracker.mapper.TaskMapper;
import com.buildermaster.projecttracker.model.*;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.repository.DeveloperRepository;
import com.buildermaster.projecttracker.repository.ProjectRepository;
import com.buildermaster.projecttracker.repository.TaskRepository;
import com.buildermaster.projecttracker.service.AuditService;
import com.buildermaster.projecttracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TaskService interface
 * Provides comprehensive task management functionality with caching, validation, and audit logging
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final AuditService auditService;
    private final TaskMapper taskMapper;
    private final MetricsService metricsService;

    // ===== CRUD OPERATIONS =====

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats", "taskSummaries"}, allEntries = true)
    public TaskResponseDTO createTask(CreateTaskRequestDTO createTaskRequest) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Creating new task with title: {}", createTaskRequest.getTitle());

        // Validate project exists
        Project project = projectRepository.findById(createTaskRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", createTaskRequest.getProjectId()));

        // Validate developer exists (if provided)
        Developer developer = null;
        if (createTaskRequest.getDeveloperId() != null) {
            developer = developerRepository.findById(createTaskRequest.getDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer", createTaskRequest.getDeveloperId()));
        }

        // Validate due date is not in the past
        if (createTaskRequest.getDueDate().isBefore(LocalDate.now())) {
            throw new ValidationException("dueDate", "Due date cannot be in the past");
        }

        // Create task entity
        Task task = new Task();
        task.setTitle(createTaskRequest.getTitle());
        task.setDescription(createTaskRequest.getDescription());
        task.setStatus(createTaskRequest.getStatus());
        task.setDueDate(createTaskRequest.getDueDate());
        task.setProject(project);
        task.setDeveloper(developer);

        // Save task
        Task savedTask = taskRepository.save(task);
        log.info("Successfully created task with ID: {}", savedTask.getId());

        // Log audit
        auditService.logAction(EActionType.CREATE, "Task", savedTask.getId(), "SYSTEM", savedTask);

        metricsService.incrementTasksCreated();

        return taskMapper.toResponseDTO(savedTask);
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskResponseDTO getTaskById(UUID taskId) {
        long startTime = System.currentTimeMillis();
        try {
        log.info("Cache MISS - Fetching task from database with ID: {}", taskId);
        log.debug("Fetching task with ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        metricsService.incrementTasksRetrieved();

        return taskMapper.toResponseDTO(task);
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Cacheable(value = "tasks", key = "'all_paginated_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> taskPage = taskRepository.findAll(pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    @Transactional
    @CachePut(value = "tasks", key = "#taskId")
    public TaskResponseDTO updateTask(UUID taskId, UpdateTaskRequestDTO updateTaskRequest) {
        long startTime = System.currentTimeMillis();
        try {
        log.info("Updating task with ID: {}", taskId);

        // Fetch existing task
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        // Validate project exists
        Project project = projectRepository.findById(updateTaskRequest.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", updateTaskRequest.getProjectId()));

        // Validate developer exists
        Developer developer = developerRepository.findById(updateTaskRequest.getDeveloperId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer", updateTaskRequest.getDeveloperId()));

        // Validate due date is not in the past (unless task is already completed)
        if (updateTaskRequest.getDueDate().isBefore(LocalDate.now()) &&
                !updateTaskRequest.getStatus().equals(ETaskStatus.COMPLETED)) {
            throw new ValidationException("dueDate", "Due date cannot be in the past for non-completed tasks");
        }

        // Update task fields
        existingTask.setTitle(updateTaskRequest.getTitle());
        existingTask.setDescription(updateTaskRequest.getDescription());
        existingTask.setStatus(updateTaskRequest.getStatus());
        existingTask.setDueDate(updateTaskRequest.getDueDate());
        existingTask.setProject(project);
        existingTask.setDeveloper(developer);

        // Save updated task
        Task updatedTask = taskRepository.save(existingTask);
        log.info("Successfully updated task with ID: {}", updatedTask.getId());

        // Log audit
        auditService.logAction(EActionType.UPDATE, "Task", updatedTask.getId(), "SYSTEM", updatedTask);

        metricsService.incrementTasksUpdated();

        return taskMapper.toResponseDTO(updatedTask);
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats", "taskSummaries"}, allEntries = true)
    public boolean deleteTask(UUID taskId) {
        long startTime = System.currentTimeMillis();
        try {
        log.info("Deleting task with ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if(task != null) {
            taskRepository.delete(task);
            log.info("Successfully deleted task with ID: {}", taskId);
            metricsService.incrementTasksDeleted();
            // Log audit
            auditService.logAction(EActionType.DELETE, "Task", taskId, "SYSTEM", task);

            return true;
        }

        return false;
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    // ===== BUSINESS OPERATIONS =====

    @Override
    public Page<TaskResponseDTO> getTasksByProjectId(UUID projectId, Pageable pageable) {
        log.debug("Fetching tasks for project ID: {} with pagination", projectId);

        validateProjectExists(projectId);
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getTasksByDeveloperId(UUID developerId, Pageable pageable) {
        log.debug("Fetching tasks for developer ID: {} with pagination", developerId);

        validateDeveloperExists(developerId);
        Page<Task> taskPage = taskRepository.findByDeveloperId(developerId, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getTasksByStatus(ETaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {} with pagination", status);

        Page<Task> taskPage = taskRepository.findByStatus(status, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getOverdueTasks(Pageable pageable) {
        log.debug("Fetching overdue tasks with pagination");

        Page<Task> taskPage = taskRepository.findByDueDateBefore(LocalDate.now(), pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getUnassignedTasks(Pageable pageable) {
        log.debug("Fetching unassigned tasks with pagination");

        List<Task> unassignedTasks = taskRepository.findUnassignedTasks();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), unassignedTasks.size());

        List<TaskResponseDTO> pageContent = unassignedTasks.subList(start, end)
                .stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, unassignedTasks.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats", "taskSummaries"}, allEntries = true)
    public TaskResponseDTO assignTaskToDeveloper(UUID taskId, UUID developerId) {
        long startTime = System.currentTimeMillis();
        try {
        log.info("Assigning task {} to developer {}", taskId, developerId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", developerId));

        task.setDeveloper(developer);
        Task updatedTask = taskRepository.save(task);

        log.info("Successfully assigned task {} to developer {}", taskId, developerId);

        // Log audit
        auditService.logAction(EActionType.UPDATE, "Task", taskId, "SYSTEM",
                Map.of("action", "assigned", "developerId", developerId));

        metricsService.incrementTasksAssigned();
        return taskMapper.toResponseDTO(updatedTask);
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats", "taskSummaries"}, allEntries = true)
    public TaskResponseDTO unassignTask(UUID taskId) {
        long startTime = System.currentTimeMillis();
        try {
        log.info("Unassigning task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        task.setDeveloper(null);
        Task updatedTask = taskRepository.save(task);

        log.info("Successfully unassigned task {}", taskId);

        // Log audit
        auditService.logAction(EActionType.UPDATE, "Task", taskId, "SYSTEM",
                Map.of("action", "unassigned"));

        metricsService.incrementTasksUnassigned();
        return taskMapper.toResponseDTO(updatedTask);
        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Page<TaskResponseDTO> searchTasksByTitle(String title, Pageable pageable) {
        log.debug("Searching tasks by title: {}", title);

        Page<Task> taskPage = taskRepository.findByTitleContainingIgnoreCase(title, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> searchTasksByDescription(String description, Pageable pageable) {
        log.debug("Searching tasks by description: {}", description);

        Page<Task> taskPage = taskRepository.findByDescriptionContainingIgnoreCase(description, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public Page<TaskResponseDTO> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching tasks by due date range: {} to {} with pagination", startDate, endDate);

        validateDateRange(startDate, endDate);
        Page<Task> taskPage = taskRepository.findByDueDateBetween(startDate, endDate, pageable);
        return taskPage.map(taskMapper::toResponseDTO);
    }

    @Override
    public List<TaskResponseDTO> getTasksDueToday() {
        log.debug("Fetching tasks due today");

        List<Task> tasks = taskRepository.findTasksDueToday();
        return tasks.stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getTasksDueThisWeek() {
        log.debug("Fetching tasks due this week");

        LocalDate now = LocalDate.now();
        LocalDate nextWeek = now.plusDays(7);

        List<Task> tasks = taskRepository.findTasksDueThisWeek(now, nextWeek);
        return tasks.stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getRecentlyCreatedTasks(LocalDateTime sinceDate) {
        log.debug("Fetching recently created tasks since: {}", sinceDate);

        List<Task> tasks = taskRepository.findRecentlyCreatedTasks(sinceDate);
        return tasks.stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getRecentlyUpdatedTasks(LocalDateTime sinceDate) {
        log.debug("Fetching recently updated tasks since: {}", sinceDate);

        List<Task> tasks = taskRepository.findRecentlyUpdatedTasks(sinceDate);
        return tasks.stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ===== STATISTICS AND ANALYTICS =====

    @Override
    @Cacheable(value = "taskStats", key = "'statusCounts'")
    public Map<ETaskStatus, Long> getTaskCountsByStatus() {
        log.debug("Fetching task counts by status");

        List<Object[]> results = taskRepository.countTasksByStatus();
        Map<ETaskStatus, Long> statusCounts = new HashMap<>();

        for (Object[] result : results) {
            ETaskStatus status = (ETaskStatus) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }

        return statusCounts;
    }

    @Override
    @Cacheable(value = "taskStats", key = "'general'")
    public TaskStatsDTO getTaskStatistics() {
        long startTime = System.currentTimeMillis();
        try {

        log.debug("Fetching general task statistics");

        long totalTasks = taskRepository.count();
        long overdueTasks = taskRepository.findOverdueTasks().size();
        long unassignedTasks = taskRepository.findUnassignedTasks().size();

        Map<String, Long> tasksByStatus = getTaskCountsByStatus().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue
                ));

        metricsService.recordTaskStatisticsGenerated();

        return TaskStatsDTO.builder()
                .totalTasks(totalTasks)
                .tasksByStatus(tasksByStatus)
                .overdueTasks(overdueTasks)
                .unassignedTasks(unassignedTasks)
                .build();

        } finally {
            metricsService.recordTaskProcessingTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public TaskStatsDTO getTaskStatisticsByProject(UUID projectId) {
        log.debug("Fetching task statistics for project: {}", projectId);

        validateProjectExists(projectId);
        List<Object[]> resultList = taskRepository.getTaskStatisticsByProject(projectId);
        return taskMapper.toStatsDTO(resultList);
    }

    @Override
    public TaskStatsDTO getTaskStatisticsByDeveloper(UUID developerId) {
        log.debug("Fetching task statistics for developer: {}", developerId);

        validateDeveloperExists(developerId);
        List<Object[]> resultList = taskRepository.getTaskStatisticsByDeveloper(developerId);
        return taskMapper.toStatsDTO(resultList);
    }

    @Override
    public Long getTaskCountByProject(UUID projectId) {
        log.debug("Fetching task count for project: {}", projectId);

        validateProjectExists(projectId);
        return taskRepository.countByProjectId(projectId);
    }

    @Override
    public Long getTaskCountByDeveloper(UUID developerId) {
        log.debug("Fetching task count for developer: {}", developerId);

        validateDeveloperExists(developerId);
        return taskRepository.countByDeveloperId(developerId);
    }

    // ===== SUMMARY OPERATIONS =====

    @Override
    public Page<TaskSummaryDTO> getTaskSummariesByProject(UUID projectId, Pageable pageable) {
        log.debug("Fetching task summaries for project: {}", projectId);

        validateProjectExists(projectId);
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);
        return taskPage.map(taskMapper::toSummaryDTO);
    }

    @Override
    public Page<TaskSummaryDTO> getTaskSummariesByDeveloper(UUID developerId, Pageable pageable) {
        log.debug("Fetching task summaries for developer: {}", developerId);

        validateDeveloperExists(developerId);
        Page<Task> taskPage = taskRepository.findByDeveloperId(developerId, pageable);
        return taskPage.map(taskMapper::toSummaryDTO);
    }

    @Override
    public Page<TaskSummaryDTO> getTaskSummariesByStatus(ETaskStatus status, Pageable pageable) {
        log.debug("Fetching task summaries for status: {}", status);

        Page<Task> taskPage = taskRepository.findByStatus(status, pageable);
        return taskPage.map(taskMapper::toSummaryDTO);
    }

    // ===== HELPER METHODS =====


    private void validateProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }

    private void validateDeveloperExists(UUID developerId) {
        if (!developerRepository.existsById(developerId)) {
            throw new ResourceNotFoundException("Developer", developerId);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Date range validation failed: start and end dates are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Date range validation failed: start date cannot be after end date");
        }
    }
}
