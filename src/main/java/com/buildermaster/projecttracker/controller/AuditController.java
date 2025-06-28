package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.AuditLogResponseDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Audit Management", description = "APIs for managing and retrieving audit logs")
public class AuditController {

    private final AuditService auditService;

    // Common parameter annotations extracted as constants
    private static final String PAGE_PARAM_DESC = "Page number (0-based)";
    private static final String SIZE_PARAM_DESC = "Page size";
    private static final String SORT_BY_PARAM_DESC = "Sort field";
    private static final String SORT_DIR_PARAM_DESC = "Sort direction";

    @Operation(
            summary = "Get entity audit trail",
            description = "Retrieve paginated audit trail for a specific entity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit trail retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getEntityAuditTrail(
            @Parameter(description = "Type of entity", example = "Project")
            @PathVariable String entityType,

            @Parameter(description = "Entity unique identifier")
            @PathVariable UUID entityId,

            @Parameter(description = PAGE_PARAM_DESC)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = SIZE_PARAM_DESC)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = SORT_BY_PARAM_DESC)
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = SORT_DIR_PARAM_DESC)
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving audit trail for entity: {} with ID: {}", entityType, entityId);

        return executeWithPagination(
                pageable -> auditService.getAuditTrail(entityType, entityId, pageable),
                "Retrieved audit trail for " + entityType,
                "No audit trail found for " + entityType,
                page, size, sortBy, sortDir
        );
    }

    @Operation(summary = "Get user action history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User actions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{actorName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getUserActions(
            @Parameter(description = "Username or actor name", example = "admin@example.com")
            @PathVariable String actorName,

            @Parameter(description = PAGE_PARAM_DESC)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = SIZE_PARAM_DESC)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = SORT_BY_PARAM_DESC)
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = SORT_DIR_PARAM_DESC)
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving actions for user: {}", actorName);

        return executeWithPagination(
                pageable -> auditService.getUserActions(actorName, pageable),
                "User actions retrieved successfully",
                "No actions found for user " + actorName,
                page, size, sortBy, sortDir
        );
    }

    @Operation(
            summary = "Get actions by type",
            description = "Retrieve paginated audit logs filtered by action type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid action type")
    })
    @GetMapping("/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getActionsByType(
            @Parameter(description = "Type of action", required = true, example = "CREATE")
            @RequestParam EActionType actionType,

            @Parameter(description = PAGE_PARAM_DESC)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = SIZE_PARAM_DESC)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = SORT_BY_PARAM_DESC)
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = SORT_DIR_PARAM_DESC)
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving actions by type: {}", actionType);

        return executeWithPagination(
                pageable -> auditService.getActionsByType(actionType, pageable),
                "Actions by type retrieved successfully",
                "No " + actionType + " actions found",
                page, size, sortBy, sortDir
        );
    }

    @Operation(summary = "Get audits by date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audits retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getAuditsByDateRange(
            @Parameter(description = "Start date and time", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,

            @Parameter(description = "End date and time", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,

            @Parameter(description = PAGE_PARAM_DESC)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = SIZE_PARAM_DESC)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = SORT_BY_PARAM_DESC)
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = SORT_DIR_PARAM_DESC)
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving audits from {} to {}", start, end);

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Start date cannot be after end date", 400));
        }

        return executeWithPagination(
                pageable -> auditService.getAuditsByDateRange(start, end, pageable),
                "Audits by date range retrieved successfully",
                "No audits found in the specified date range",
                page, size, sortBy, sortDir
        );
    }

    @Operation(summary = "Get recent audit activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent audits retrieved successfully")
    })
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> getRecentAudits(
            @Parameter(description = PAGE_PARAM_DESC)
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = SIZE_PARAM_DESC)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = SORT_BY_PARAM_DESC)
            @RequestParam(defaultValue = "timestamp") String sortBy,

            @Parameter(description = SORT_DIR_PARAM_DESC)
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Retrieving recent audit activities");

        return executeWithPagination(
                auditService::getAllAudits,
                "Recent audit activities retrieved successfully",
                "No recent audit activities found",
                page, size, sortBy, sortDir
        );
    }

    /**
     * Common method to handle pagination for all audit endpoints with conditional responses
     */
    private ResponseEntity<ApiResponseDTO<Page<AuditLogResponseDTO>>> executeWithPagination(
            Function<Pageable, Page<AuditLogResponseDTO>> serviceCall,
            String successMessage,
            String emptyMessage,
            int page, int size, String sortBy, String sortDir) {

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<AuditLogResponseDTO> result = serviceCall.apply(pageable);

        return result != null && result.hasContent()
                ? ResponseEntity.ok(ApiResponseDTO.success(successMessage, result))
                : ResponseEntity.ok(ApiResponseDTO.success(emptyMessage, result));
    }

    /**
     * Creates Pageable object with sorting
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC, sortBy);
        return PageRequest.of(page, size, sort);
    }
}