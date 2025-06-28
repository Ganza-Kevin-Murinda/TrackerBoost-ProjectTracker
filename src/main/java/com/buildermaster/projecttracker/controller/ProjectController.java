package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateProjectRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.model.EProjectStatus;
import com.buildermaster.projecttracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "APIs for managing projects in the system")
public class ProjectController {

    private final ProjectService projectService;

    // ===== CRUD OPERATIONS =====

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Project with same name already exists")
    })
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> createProject(
            @Valid @RequestBody CreateProjectRequestDTO createRequest) {
        log.info("Creating new project: {}", createRequest.getName());
        ProjectResponseDTO project = projectService.createProject(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(project));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get all projects with pagination")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdDate") Pageable pageable) {
        log.info("Retrieving projects - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ProjectSummaryDTO> projects = projectService.getAllProjects(pageable);
        return projects.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects retrieved successfully", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found", projects));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get project by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> getProjectById(@PathVariable UUID id) {
        log.info("Retrieving project: {}", id);
        ProjectResponseDTO project = projectService.getProjectById(id);
        return project != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Project retrieved successfully", project))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Project not found", 404));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Update project")
    public ResponseEntity<ApiResponseDTO<ProjectResponseDTO>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequestDTO updateRequest) {
        log.info("Updating project: {}", id);
        ProjectResponseDTO project = projectService.updateProject(id, updateRequest);
        return project != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Project updated successfully", project))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Project not found", 404));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProject(@PathVariable UUID id) {
        log.info("Deleting project: {}", id);
        boolean deleted = projectService.deleteProject(id);
        return deleted
                ? ResponseEntity.ok(ApiResponseDTO.success("Project deleted successfully", null))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Project not found", 404));
    }

    // ===== SPECIALIZED ENDPOINTS =====

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get projects by status")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsByStatus(
            @PathVariable EProjectStatus status,
            @PageableDefault(size = 10, sort = "deadline") Pageable pageable) {
        log.info("Retrieving projects with status: {}", status);
        Page<ProjectSummaryDTO> projects = projectService.getProjectsByStatus(status, pageable);
        return projects.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects by status retrieved successfully", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found with this status", projects));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get overdue projects")
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getOverdueProjects() {
        log.info("Retrieving overdue projects");
        List<ProjectResponseDTO> projects = projectService.getOverdueProjects();
        return !projects.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Overdue projects retrieved successfully", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No overdue projects found", projects));
    }

    @GetMapping("/empty")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get projects without tasks")
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getProjectsWithoutTasks() {
        log.info("Retrieving projects without tasks");
        List<ProjectResponseDTO> projects = projectService.getProjectsWithoutTasks();
        return !projects.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects without tasks retrieved successfully", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects without tasks found", projects));
    }

    @GetMapping("/by-task-count")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get projects ordered by task count")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getProjectsOrderedByTaskCount(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Retrieving projects ordered by task count");
        Page<ProjectSummaryDTO> projects = projectService.getProjectsOrderedByTaskCount(pageable);
        return projects.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects ordered by task count retrieved", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found", projects));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Search projects", description = "Search projects by name or description")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> searchProjects(
            @RequestParam String term,
            @RequestParam(defaultValue = "name") String type,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        log.info("Searching projects by {}: {}", type, term);
        Page<ProjectSummaryDTO> projects = "description".equals(type)
                ? projectService.searchProjectsByDescription(term, pageable)
                : projectService.searchProjectsByName(term, pageable);
        return projects.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects found", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found matching search criteria", projects));
    }

    @GetMapping("/deadline-range")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get projects by deadline range")
    public ResponseEntity<ApiResponseDTO<List<ProjectResponseDTO>>> getProjectsByDeadlineRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Retrieving projects with deadlines between {} and {}", startDate, endDate);
        List<ProjectResponseDTO> projects = projectService.getProjectsByDeadlineRange(startDate, endDate);
        return !projects.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects in deadline range retrieved", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found in deadline range", projects));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('CONTRACTOR')")
    @Operation(summary = "Get recent projects", description = "Get recently created or updated projects")
    public ResponseEntity<ApiResponseDTO<Page<ProjectSummaryDTO>>> getRecentProjects(
            @RequestParam(defaultValue = "created") String sortBy,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Retrieving recent projects sorted by: {}", sortBy);
        Page<ProjectSummaryDTO> projects = "updated".equals(sortBy)
                ? projectService.getRecentlyUpdatedProjects(pageable)
                : projectService.getRecentlyCreatedProjects(pageable);
        return projects.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Recent projects retrieved", projects))
                : ResponseEntity.ok(ApiResponseDTO.success("No recent projects found", projects));
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/exists")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Check if project exists by name")
    public ResponseEntity<ApiResponseDTO<Boolean>> existsByName(@RequestParam String name) {
        log.info("Checking if project exists: {}", name);
        boolean exists = projectService.existsByName(name);
        return ResponseEntity.ok(ApiResponseDTO.success("Project existence check completed", exists));
    }

    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get project count by status")
    public ResponseEntity<ApiResponseDTO<Long>> getProjectCountByStatus(@PathVariable EProjectStatus status) {
        log.info("Getting project count for status: {}", status);
        Long count = projectService.getProjectCountByStatus(status);
        return count != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Project count by status retrieved", count))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects found with this status", 0L));
    }

    @GetMapping("/count/with-tasks")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get count of projects with tasks")
    public ResponseEntity<ApiResponseDTO<Long>> getProjectsWithTasksCount() {
        log.info("Getting count of projects with tasks");
        Long count = projectService.getProjectsWithTasksCount();
        return count != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Projects with tasks count retrieved", count))
                : ResponseEntity.ok(ApiResponseDTO.success("No projects with tasks found", 0L));
    }
}