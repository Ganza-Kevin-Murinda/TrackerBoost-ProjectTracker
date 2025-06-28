package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.EActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing audit log information")
public class AuditLogResponseDTO {

    @Schema(description = "Unique audit log identifier")
    private String id;

    @Schema(description = "Type of action performed", example = "CREATE")
    private EActionType actionType;

    @Schema(description = "Human-readable action description", example = "Created a new project")
    private String actionDescription;

    @Schema(description = "Type of entity affected", example = "Project")
    private String entityType;

    @Schema(description = "ID of the affected entity")
    private UUID entityId;

    @Schema(description = "Timestamp when action was performed")
    private LocalDateTime timestamp;

    @Schema(description = "Formatted timestamp for display", example = "2024-06-06 14:30:25")
    private String formattedTimestamp;

    @Schema(description = "Name of the user who performed the action", example = "admin@example.com")
    private String actorName;

    @Schema(description = "Additional data related to the action")
    private Map<String, Object> payload;

    @Schema(description = "Summary of changes made", example = "Name changed from 'Old Project' to 'New Project'")
    private String changesSummary;
}