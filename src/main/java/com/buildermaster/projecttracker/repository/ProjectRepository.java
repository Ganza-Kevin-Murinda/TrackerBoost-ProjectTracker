package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Project;
import com.buildermaster.projecttracker.model.EProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Project entity
 * Provides CRUD operations and custom query methods for Project management
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // ===== DERIVED QUERY METHODS =====

    List<Project> findByStatus(EProjectStatus status);

    List<Project> findByDeadlineBetween(LocalDate start, LocalDate end);

    boolean existsByNameIgnoreCase(String name);

    // ===== CUSTOM @QUERY METHODS =====

    @Query("SELECT p FROM Project p WHERE p.deadline < CURRENT_DATE AND p.status != 'COMPLETED'")
    List<Project> findOverdueProjects();

    @Query("SELECT p FROM Project p WHERE p.tasks IS EMPTY")
    List<Project> findProjectsWithoutTasks();

    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t GROUP BY p ORDER BY COUNT(t) DESC")
    Page<Project> findProjectsOrderedByTaskCount(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") EProjectStatus status);

    @Query("SELECT COUNT(DISTINCT p) FROM Project p JOIN p.tasks t")
    Long countProjectsWithTasks();

    Page<Project> findByStatus(EProjectStatus status, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<Project> findByDeadlineBefore(LocalDate date, Pageable pageable);

    Page<Project> findByDeadlineAfter(LocalDate date, Pageable pageable);

    Page<Project> findAllByOrderByCreatedDateDesc(Pageable pageable);

    Page<Project> findAllByOrderByUpdatedDateDesc(Pageable pageable);

}