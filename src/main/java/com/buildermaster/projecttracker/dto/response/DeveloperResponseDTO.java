package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing developer information")
public class DeveloperResponseDTO {

    @Schema(description = "Unique developer identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Developer's full name", example = "John Doe")
    private String name;

    @Schema(description = "Developer's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Developer's technical skills", example = "Java, Spring Boot, React, PostgreSQL")
    private String skills;

    @Schema(description = "Total number of assigned tasks", example = "12")
    private Integer totalTaskCount;

    @Schema(description = "Developer creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "Developer last update timestamp")
    private LocalDateTime updatedDate;
}
