package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary DTO for developer listings")
public class DeveloperSummaryDTO {

    @Schema(description = "Unique developer identifier")
    private UUID id;

    @Schema(description = "Developer's full name")
    private String name;

    @Schema(description = "Developer's email address")
    private String email;

    @Schema(description = "Developer's primary skills (truncated)")
    private String primarySkills;

    @Schema(description = "Total number of assigned tasks")
    private Integer totalTaskCount;

}
