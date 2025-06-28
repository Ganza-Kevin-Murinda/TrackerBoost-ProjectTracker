package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task statistics and counts")
public class TaskStatsDTO {

    @Schema(description = "Total number of tasks")
    private Long totalTasks;

    @Schema(description = "Task count by status")
    private Map<String, Long> tasksByStatus;

    @Schema(description = "Number of overdue tasks")
    private Long overdueTasks;

    @Schema(description = "Number of unassigned tasks")
    private Long unassignedTasks;
}
