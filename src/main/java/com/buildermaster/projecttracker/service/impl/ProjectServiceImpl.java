package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.dto.request.CreateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.exception.ResourceNotFoundException;
import com.buildermaster.projecttracker.exception.ValidationException;
import com.buildermaster.projecttracker.mapper.ProjectMapper;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.EProjectStatus;
import com.buildermaster.projecttracker.model.Project;
import com.buildermaster.projecttracker.repository.ProjectRepository;
import com.buildermaster.projecttracker.service.AuditService;
import com.buildermaster.projecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProjectService interface
 * Provides business logic for project management operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditService auditService;
    private final ProjectMapper projectMapper;

    private static final String SYSTEM_ACTOR = "SYSTEM";

    // ===== CRUD OPERATIONS =====

    @Override
    public ProjectResponseDTO createProject(CreateProjectRequestDTO createRequest) {
        log.info("Creating new project with name: {}", createRequest.getName());

        // Validate business rules
        validateProjectCreation(createRequest);

        // Create project entity
        Project project = new Project(
                createRequest.getName(),
                createRequest.getDescription(),
                createRequest.getDeadline(),
                createRequest.getStatus() != null ? createRequest.getStatus() : EProjectStatus.PLANNING
        );

        // Save project
        Project savedProject = projectRepository.save(project);
        log.info("Successfully created project with ID: {}", savedProject.getId());

        // Log audit
        auditService.logAction(EActionType.CREATE, "Project", savedProject.getId(), SYSTEM_ACTOR , savedProject);

        return projectMapper.toResponseDTO(savedProject);
    }

    @Override
    @Cacheable(value = "projects", key = "#id")
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(UUID id) {
        log.debug("Fetching project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {}", id);
                    return new ResourceNotFoundException("Project", id);
                });

        return projectMapper.toResponseDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> getAllProjects(Pageable pageable) {
        log.debug("Fetching all projects with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findAll(pageable);
        log.debug("found {} projects on page {} of {}",
                projectPage.getContent().size(),
                projectPage.getNumber() + 1,
                projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @CachePut(value = "projects", key = "#id")
    public ProjectResponseDTO updateProject(UUID id, UpdateProjectRequestDTO updateRequest) {
        log.info("Updating project with ID: {}", id);

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Project", id);
                });

        // Validate business rules
        validateProjectUpdate(id, updateRequest, existingProject);

        // Update project fields
        existingProject.setName(updateRequest.getName());
        existingProject.setDescription(updateRequest.getDescription());
        existingProject.setDeadline(updateRequest.getDeadline());
        existingProject.setStatus(updateRequest.getStatus());

        // Save updated project
        Project updatedProject = projectRepository.save(existingProject);
        log.info("Successfully updated project with ID: {}", updatedProject.getId());

        // Log audit
        auditService.logAction(EActionType.UPDATE, "Project", updatedProject.getId(), SYSTEM_ACTOR , updatedProject);

        return projectMapper.toResponseDTO(updatedProject);
    }

    @Override
    @CacheEvict(value = {"projects", "allProjects"}, allEntries = true)
    public boolean deleteProject(UUID id) {
        log.info("Deleting project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found for deletion with ID: {}", id);
                    return new ResourceNotFoundException("Project", id);
                });

        if(project != null) {
            // Delete project (cascade will handle tasks)
            projectRepository.delete(project);
            log.info("Successfully deleted project with ID: {} and {} associated tasks", id, project.getTasks().size());

            // Log audit
            auditService.logAction(EActionType.DELETE, "Project", id, SYSTEM_ACTOR , project);
            return true;
        }
        return false;
    }

    // ===== BUSINESS OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> getProjectsByStatus(EProjectStatus status, Pageable pageable) {
        log.debug("Fetching projects by status: {} with pagination: page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findByStatus(status, pageable);
        log.debug("Found {} projects with status: {} on page {} of {}",
                projectPage.getContent().size(), status,
                projectPage.getNumber() + 1, projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getOverdueProjects() {
        log.debug("Fetching overdue projects");

        List<Project> overdueProjects = projectRepository.findOverdueProjects();
        log.debug("Found {} overdue projects", overdueProjects.size());

        return overdueProjects.stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsWithoutTasks() {
        log.debug("Fetching projects without tasks");

        List<Project> projectsWithoutTasks = projectRepository.findProjectsWithoutTasks();
        log.debug("Found {} projects without tasks", projectsWithoutTasks.size());

        return projectsWithoutTasks.stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> getProjectsOrderedByTaskCount(Pageable pageable) {
        log.debug("Fetching projects ordered by task count with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findProjectsOrderedByTaskCount(pageable);
        log.debug("Found {} projects ordered by task count on page {} of {}",
                projectPage.getContent().size(),
                projectPage.getNumber() + 1,
                projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByDeadlineRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching projects by deadline range: {} to {}", startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("startDate", "Start date must be before or equal to end date");
        }

        List<Project> projects = projectRepository.findByDeadlineBetween(startDate, endDate);
        log.debug("Found {} projects with deadlines between {} and {}", projects.size(), startDate, endDate);

        return projects.stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> searchProjectsByName(String name, Pageable pageable) {
        log.debug("Searching projects by name: '{}' with pagination: page={}, size={}",
                name, pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findByNameContainingIgnoreCase(name, pageable);
        log.debug("Found {} projects matching name search '{}' on page {} of {}",
                projectPage.getContent().size(), name,
                projectPage.getNumber() + 1, projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> searchProjectsByDescription(String description, Pageable pageable) {
        log.debug("Searching projects by description: '{}' with pagination: page={}, size={}",
                description, pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findByDescriptionContainingIgnoreCase(description, pageable);
        log.debug("Found {} projects matching description search '{}' on page {} of {}",
                projectPage.getContent().size(), description,
                projectPage.getNumber() + 1, projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> getRecentlyCreatedProjects(Pageable pageable) {
        log.debug("Fetching recently created projects with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findAllByOrderByCreatedDateDesc(pageable);
        log.debug("Found {} recently created Projects on page {} of {}",
                projectPage.getContent().size(),
                projectPage.getNumber() + 1,
                projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectSummaryDTO> getRecentlyUpdatedProjects(Pageable pageable) {
        log.debug("Fetching recently updated projects with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findAllByOrderByUpdatedDateDesc(pageable);
        log.debug("Found {} recently updated projects on page {} of {}",
                projectPage.getContent().size(),
                projectPage.getNumber() + 1,
                projectPage.getTotalPages());

        return projectPage.map(projectMapper::toSummaryDTO);
    }

    // ===== UTILITY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        log.debug("Checking if project exists by name: {}", name);
        return projectRepository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProjectCountByStatus(EProjectStatus status) {
        log.debug("Getting project count by status: {}", status);
        return projectRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProjectsWithTasksCount() {
        log.debug("Getting count of projects with tasks");
        return projectRepository.countProjectsWithTasks();
    }

    // ===== PRIVATE HELPER METHODS =====

    private void validateProjectCreation(CreateProjectRequestDTO createRequest) {
        // Check if project name already exists
        if (projectRepository.existsByNameIgnoreCase(createRequest.getName())) {
            throw new ValidationException("name", "Project with this name already exists");
        }

        // Validate deadline is in the future
        if (createRequest.getDeadline().isBefore(LocalDate.now())) {
            throw new ValidationException("deadline", "Project deadline must be in the future");
        }
    }

    private void validateProjectUpdate(UUID id, UpdateProjectRequestDTO updateRequest, Project existingProject) {
        // Check if another project has the same name (excluding current project)
        if (!existingProject.getName().equalsIgnoreCase(updateRequest.getName()) &&
                projectRepository.existsByNameIgnoreCase(updateRequest.getName())) {
            throw new ValidationException("name", "Another project with this name already exists");
        }

        // Validate deadline is in the future
        if (updateRequest.getDeadline().isBefore(LocalDate.now())) {
            throw new ValidationException("deadline", "Project deadline must be in the future");
        }
    }

}