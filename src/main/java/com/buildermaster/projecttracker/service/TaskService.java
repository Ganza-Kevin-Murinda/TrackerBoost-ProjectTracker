package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateTaskRequestDTO;
import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Task operations
 * Provides comprehensive business methods for task management
 */
public interface TaskService {

    // ===== CRUD OPERATIONS =====

    TaskResponseDTO createTask(CreateTaskRequestDTO createTaskRequest);

    TaskResponseDTO getTaskById(UUID taskId);

    List<TaskResponseDTO> getAllTasks();

    Page<TaskResponseDTO> getAllTasks(Pageable pageable);

    TaskResponseDTO updateTask(UUID taskId, UpdateTaskRequestDTO updateTaskRequest);

    boolean deleteTask(UUID taskId);

    // ===== BUSINESS OPERATIONS =====

    Page<TaskResponseDTO> getTasksByProjectId(UUID projectId, Pageable pageable);

    Page<TaskResponseDTO> getTasksByDeveloperId(UUID developerId, Pageable pageable);

    Page<TaskResponseDTO> getTasksByStatus(ETaskStatus status, Pageable pageable);

    Page<TaskResponseDTO> getOverdueTasks(Pageable pageable);

    Page<TaskResponseDTO> getUnassignedTasks(Pageable pageable);

    TaskResponseDTO assignTaskToDeveloper(UUID taskId, UUID developerId);

    TaskResponseDTO unassignTask(UUID taskId);

    Page<TaskResponseDTO> searchTasksByTitle(String title, Pageable pageable);

    Page<TaskResponseDTO> searchTasksByDescription(String description, Pageable pageable);

    Page<TaskResponseDTO> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<TaskResponseDTO> getTasksDueToday();

    List<TaskResponseDTO> getTasksDueThisWeek();

    List<TaskResponseDTO> getRecentlyCreatedTasks(LocalDateTime sinceDate);

    List<TaskResponseDTO> getRecentlyUpdatedTasks(LocalDateTime sinceDate);

    // ===== STATISTICS AND ANALYTICS =====

    Map<ETaskStatus, Long> getTaskCountsByStatus();

    TaskStatsDTO getTaskStatistics();

    TaskStatsDTO getTaskStatisticsByProject(UUID projectId);

    TaskStatsDTO getTaskStatisticsByDeveloper(UUID developerId);

    Long getTaskCountByProject(UUID projectId);

    Long getTaskCountByDeveloper(UUID developerId);

    // ===== SUMMARY OPERATIONS =====

    Page<TaskSummaryDTO> getTaskSummariesByProject(UUID projectId, Pageable pageable);

    Page<TaskSummaryDTO> getTaskSummariesByDeveloper(UUID developerId, Pageable pageable);

    Page<TaskSummaryDTO> getTaskSummariesByStatus(ETaskStatus status, Pageable pageable);
}