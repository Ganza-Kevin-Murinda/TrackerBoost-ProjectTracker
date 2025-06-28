package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.ETaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing task information")
public class TaskResponseDTO {

    @Schema(description = "Unique task identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Task title", example = "Implement user authentication")
    private String title;

    @Schema(description = "Task description", example = "Implement JWT-based authentication with refresh tokens")
    private String description;

    @Schema(description = "Current task status", example = "IN_PROGRESS")
    private ETaskStatus status;

    @Schema(description = "Task due date", example = "2024-08-15")
    private LocalDate dueDate;

    @Schema(description = "Indicates if the task is overdue")
    private Boolean isOverdue;

    @Schema(description = "Days remaining until due date", example = "5")
    private Long daysRemaining;

    @Schema(description = "Days overdue (if applicable)", example = "0")
    private Long daysOverdue;

    // Project information
    @Schema(description = "Name of the project this task belongs to", example = "E-commerce Platform")
    private String projectName;

    // Developer information
    @Schema(description = "Name of the assigned developer", example = "John Doe")
    private String developerName;

    @Schema(description = "Task creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "Task last update timestamp")
    private LocalDateTime updatedDate;
}

