package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.model.EProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Project operations
 * Provides business logic methods for project management
 */
public interface ProjectService {

    // ===== CRUD OPERATIONS =====

    ProjectResponseDTO createProject(CreateProjectRequestDTO createRequest);

    ProjectResponseDTO getProjectById(UUID id);

    Page<ProjectSummaryDTO> getAllProjects(Pageable pageable);

    ProjectResponseDTO updateProject(UUID id, UpdateProjectRequestDTO updateRequest);

    boolean deleteProject(UUID id);

    // ===== BUSINESS OPERATIONS =====

    Page<ProjectSummaryDTO> getProjectsByStatus(EProjectStatus status, Pageable pageable);

    List<ProjectResponseDTO> getOverdueProjects();

    List<ProjectResponseDTO> getProjectsWithoutTasks();

    Page<ProjectSummaryDTO> getProjectsOrderedByTaskCount(Pageable pageable);

    List<ProjectResponseDTO> getProjectsByDeadlineRange(LocalDate startDate, LocalDate endDate);

    Page<ProjectSummaryDTO> searchProjectsByName(String name, Pageable pageable);

    Page<ProjectSummaryDTO> searchProjectsByDescription(String description, Pageable pageable);

    Page<ProjectSummaryDTO> getRecentlyCreatedProjects(Pageable pageable);

    Page<ProjectSummaryDTO> getRecentlyUpdatedProjects(Pageable pageable);

    // ===== UTILITY METHODS =====

    boolean existsByName(String name);

    Long getProjectCountByStatus(EProjectStatus status);

    Long getProjectsWithTasksCount();
}
