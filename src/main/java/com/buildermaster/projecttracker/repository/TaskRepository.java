package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Task;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository interface for Task entity
 * Provides CRUD operations and custom query methods for Task management
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // ===== DERIVED QUERY METHODS =====

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByDeveloperId(UUID developerId);

    List<Task> findByDueDateBetween(LocalDate start, LocalDate end);

    Long countByProjectId(UUID projectId);

    Long countByDeveloperId(UUID developerId);

    // ===== CUSTOM @QUERY METHODS =====

    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks();

    @Query("SELECT t FROM Task t WHERE t.developer IS NULL")
    List<Task> findUnassignedTasks();

    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    // ===== PAGINATION SUPPORT METHODS =====

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    Page<Task> findByDeveloperId(UUID developerId, Pageable pageable);

    Page<Task> findByStatus(ETaskStatus status, Pageable pageable);

    Page<Task> findByDueDateBefore(LocalDate date, Pageable pageable);

    Page<Task> findByDueDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Task> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // ===== ADDITIONAL USEFUL QUERY METHODS =====

    @Query("SELECT t FROM Task t WHERE t.dueDate = CURRENT_DATE")
    List<Task> findTasksDueToday();

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :now AND :nextWeek")
    List<Task> findTasksDueThisWeek(@Param("now") LocalDate now, @Param("nextWeek") LocalDate nextWeek);

    @Query("SELECT t FROM Task t WHERE t.createdDate >= :sinceDate")
    List<Task> findRecentlyCreatedTasks(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT t FROM Task t WHERE t.updatedDate >= :sinceDate")
    List<Task> findRecentlyUpdatedTasks(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
            "SUM(CASE WHEN t.status = 'BLOCKED' THEN 1 ELSE 0 END) as blocked " +
            "FROM Task t WHERE t.project.id = :projectId")
    List<Object[]> getTaskStatisticsByProject(@Param("projectId") UUID projectId);

    @Query("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
            "SUM(CASE WHEN t.status = 'BLOCKED' THEN 1 ELSE 0 END) as blocked " +
            "FROM Task t WHERE t.developer.id = :developerId")
    List<Object[]> getTaskStatisticsByDeveloper(@Param("developerId") UUID developerId);
}
