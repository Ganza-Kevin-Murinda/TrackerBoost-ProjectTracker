package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperStatsDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.service.DeveloperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing developers
 */
@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Developer Management", description = "APIs for managing developers and their information")
public class DeveloperController {

    private final DeveloperService developerService;

    // ===== CRUD ENDPOINTS =====

    @PostMapping
    @PreAuthorize("hasRole('DEVELOPER')or hasRole('ADMIN')")
    @Operation(summary = "Create a new developer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Developer Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> createDeveloperProfile(
            @Valid @RequestBody CreateDeveloperRequestDTO createRequest) {

        log.info("Creating developer profile with email: {}", createRequest.getEmail());
        DeveloperResponseDTO createdDeveloper = developerService.createDeveloperProfile(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(createdDeveloper));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all developers with pagination")
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> getAllDevelopers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DeveloperResponseDTO> developers = developerService.getAllDevelopers(pageable);

        return developers.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Developers retrieved successfully", developers))
                : ResponseEntity.ok(ApiResponseDTO.success("No developers found", developers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get developer by ID")
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> getDeveloperById(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id) {

        DeveloperResponseDTO developer = developerService.getDeveloperById(id);
        return developer != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer retrieved successfully", developer))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DEVELOPER')or hasRole('ADMIN')")
    @Operation(summary = "Update developer profile")
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> updateDeveloperProfile(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id,
            @Valid @RequestBody UpdateDeveloperRequestDTO updateRequest) {

        log.info("Updating developer profile with ID: {}", id);
        DeveloperResponseDTO updatedDeveloper = developerService.updateDeveloperProfile(id, updateRequest);
        return updatedDeveloper != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer profile updated successfully", updatedDeveloper))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')or hasRole('ADMIN')")
    @Operation(summary = "Delete developer profile")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDeveloperProfile(
            @Parameter(description = "Developer unique identifier") @PathVariable UUID id) {

        log.info("Deleting developer profile with ID: {}", id);
        boolean deleted = developerService.deleteDeveloperProfile(id);
        return deleted
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer deleted successfully", null))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    // ===== SPECIALIZED ENDPOINTS =====

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Find developer by email")
    public ResponseEntity<ApiResponseDTO<DeveloperResponseDTO>> getDeveloperByEmail(
            @Parameter(description = "Developer email address") @PathVariable String email) {

        DeveloperResponseDTO developer = developerService.getDeveloperByEmail(email);
        return developer != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Developer retrieved successfully", developer))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    @GetMapping("/top-performers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top 5 performing developers by task count")
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getTopPerformers() {

        List<DeveloperSummaryDTO> topDevelopers = developerService.getTop5DevelopersByTaskCount();
        return !topDevelopers.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Top performers retrieved successfully", topDevelopers))
                : ResponseEntity.ok(ApiResponseDTO.success("No top performers found", topDevelopers));
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get developers without assigned tasks")
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getAvailableDevelopers() {

        List<DeveloperSummaryDTO> availableDevelopers = developerService.getDevelopersWithoutTasks();
        return !availableDevelopers.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Available developers retrieved successfully", availableDevelopers))
                : ResponseEntity.ok(ApiResponseDTO.success("No available developers found", availableDevelopers));
    }

    @GetMapping("/by-task-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get developers by task status")
    public ResponseEntity<ApiResponseDTO<List<DeveloperSummaryDTO>>> getDevelopersByTaskStatus(
            @Parameter(description = "Task status filter") @PathVariable ETaskStatus status) {

        List<DeveloperSummaryDTO> developers = developerService.getDevelopersByTaskStatus(status);
        return !developers.isEmpty()
                ? ResponseEntity.ok(ApiResponseDTO.success("Developers retrieved successfully", developers))
                : ResponseEntity.ok(ApiResponseDTO.success("No developers found with this task status", developers));
    }

    @GetMapping("/search/name")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search developers by name")
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> searchDevelopersByName(
            @Parameter(description = "Search term") @RequestParam String name,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DeveloperResponseDTO> searchResults = developerService.searchDevelopersByName(name, pageable);

        return searchResults.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Search completed successfully", searchResults))
                : ResponseEntity.ok(ApiResponseDTO.success("No developers found matching search criteria", searchResults));
    }

    @GetMapping("/search/skill")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search developers by skill")
    public ResponseEntity<ApiResponseDTO<Page<DeveloperResponseDTO>>> searchDevelopersBySkill(
            @Parameter(description = "Search term") @RequestParam String skill,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DeveloperResponseDTO> searchResults = developerService.searchDevelopersBySkill(skill, pageable);

        return searchResults.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success("Search completed successfully", searchResults))
                : ResponseEntity.ok(ApiResponseDTO.success("No developers found matching search criteria", searchResults));
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get developer statistics")
    public ResponseEntity<ApiResponseDTO<DeveloperStatsDTO>> getDeveloperStats() {

        Long totalCount = developerService.getTotalDeveloperCount();
        Long withTasksCount = developerService.getDeveloperCountWithTasks();

        if (totalCount != null && withTasksCount != null) {
            Long availableCount = totalCount - withTasksCount;
            DeveloperStatsDTO stats = DeveloperStatsDTO.builder()
                    .totalDevelopers(totalCount)
                    .developersWithTasks(withTasksCount)
                    .availableDevelopers(availableCount)
                    .build();
            return ResponseEntity.ok(ApiResponseDTO.success("Statistics retrieved successfully", stats));
        }

        return ResponseEntity.ok(ApiResponseDTO.success("No statistics available", null));
    }
}