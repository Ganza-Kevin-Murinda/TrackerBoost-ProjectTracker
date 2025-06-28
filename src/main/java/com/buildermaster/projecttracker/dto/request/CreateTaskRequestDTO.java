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
@Schema(description = "Request DTO for creating a new task")
public class CreateTaskRequestDTO {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    @Schema(description = "Title of the task", example = "Implement user authentication")
    private String title;

    @NotBlank(message = "Task description is required")
    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    @Schema(description = "Detailed description of the task", example = "Implement JWT-based authentication with refresh tokens")
    private String description;

    @NotNull(message = "Task status is required")
    @Schema(description = "Initial task status", example = "TODO")
    private ETaskStatus status;

    @NotNull(message = "Due date is required")
    @Schema(description = "Task due date", example = "2024-08-15")
    private LocalDate dueDate;

    @NotNull(message = "Project ID is required")
    @Schema(description = "ID of the project this task belongs to")
    private UUID projectId;

    @NotNull(message = "Developer ID is required")
    @Schema(description = "ID of the developer assigned to this task (optional)")
    private UUID developerId;
}