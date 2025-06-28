package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response structure")
public class ErrorResponseDTO {

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "HTTP status reason", example = "Bad Request")
    private String error;

    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/projects")
    private String path;

    @Schema(description = "Field-specific validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "Additional error details")
    private Map<String, Object> details;
}
