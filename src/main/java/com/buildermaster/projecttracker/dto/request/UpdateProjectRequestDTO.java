package com.buildermaster.projecttracker.dto.request;

import com.buildermaster.projecttracker.model.EProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating an existing project")
public class UpdateProjectRequestDTO {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    @Schema(description = "Updated name of the project", example = "E-commerce Platform v2")
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(max = 500, message = "Project description cannot exceed 500 characters")
    @Schema(description = "Updated description of the project")
    private String description;

    @NotNull(message = "Project deadline is required")
    @Future(message = "Project deadline must be in the future")
    @Schema(description = "Updated project deadline", example = "2024-12-31")
    private LocalDate deadline;

    @NotNull(message = "Project status is required")
    @Schema(description = "Updated project status", example = "IN_PROGRESS")
    private EProjectStatus status;
}
