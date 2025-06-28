package com.buildermaster.projecttracker.dto.request;

import com.buildermaster.projecttracker.model.ETaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating an existing task")
public class UpdateTaskRequestDTO {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    @Schema(description = "Updated title of the task", example = "Implement user authentication with 2FA")
    private String title;

    @NotBlank(message = "Task description is required")
    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    @Schema(description = "Updated description of the task")
    private String description;

    @NotNull(message = "Task status is required")
    @Schema(description = "Updated task status", example = "IN_PROGRESS")
    private ETaskStatus status;

    @NotNull(message = "Due date is required")
    @Schema(description = "Updated due date", example = "2024-08-20")
    private LocalDate dueDate;

    @NotNull(message = "Project ID is required")
    @Schema(description = "Updated project ID (if task needs to be moved)")
    private UUID projectId;

    @NotNull(message = "Developer ID is required")
    @Schema(description = "Updated developer ID (for reassignment)")
    private UUID developerId;
}

