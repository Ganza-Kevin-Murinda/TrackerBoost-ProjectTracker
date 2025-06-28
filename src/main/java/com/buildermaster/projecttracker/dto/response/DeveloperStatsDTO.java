package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Developer statistics summary")
public class DeveloperStatsDTO {

    @Schema(description = "Total number of developers")
    private Long totalDevelopers;

    @Schema(description = "Number of developers with assigned tasks")
    private Long developersWithTasks;

    @Schema(description = "Number of available developers")
    private Long availableDevelopers;
}