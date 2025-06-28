package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.ETaskStatus;
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
@Schema(description = "Summary DTO for task listings")
public class TaskSummaryDTO {

    @Schema(description = "Unique task identifier")
    private UUID id;

    @Schema(description = "Task title")
    private String title;

    @Schema(description = "Current task status")
    private ETaskStatus status;

    @Schema(description = "Task due date")
    private LocalDate dueDate;

    @Schema(description = "Indicates if the task is overdue")
    private Boolean isOverdue;

    @Schema(description = "Name of the project this task belongs to")
    private String projectName;

    @Schema(description = "Name of the assigned developer")
    private String developerName;
}
