package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.EProjectStatus;
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
@Schema(description = "Response DTO containing project information")
public class ProjectResponseDTO {

    @Schema(description = "Unique project identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project name", example = "E-commerce Platform")
    private String name;

    @Schema(description = "Project description", example = "Modern e-commerce platform with microservices architecture")
    private String description;

    @Schema(description = "Project deadline", example = "2024-12-31")
    private LocalDate deadline;

    @Schema(description = "Current project status", example = "IN_PROGRESS")
    private EProjectStatus status;

    @Schema(description = "Number of tasks in this project", example = "15")
    private Integer taskCount;

    @Schema(description = "Number of completed tasks", example = "8")
    private Integer completedTaskCount;

    @Schema(description = "Project completion percentage", example = "53.33")
    private Double completionPercentage;

    @Schema(description = "Indicates if the project is overdue")
    private Boolean isOverdue;

    @Schema(description = "Project creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "Project last update timestamp")
    private LocalDateTime updatedDate;
}
