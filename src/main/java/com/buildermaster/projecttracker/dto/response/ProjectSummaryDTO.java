package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.EProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary DTO for project listings")
public class ProjectSummaryDTO {

    @Schema(description = "Unique project identifier")
    private UUID id;

    @Schema(description = "Project name")
    private String name;

    @Schema(description = "Project status")
    private EProjectStatus status;

    @Schema(description = "Project deadline")
    private LocalDate deadline;

    @Schema(description = "Number of tasks in this project")
    private Integer taskCount;

    @Schema(description = "Project completion percentage")
    private Double completionPercentage;

    @Schema(description = "Indicates if the project is overdue")
    private Boolean isOverdue;
}
