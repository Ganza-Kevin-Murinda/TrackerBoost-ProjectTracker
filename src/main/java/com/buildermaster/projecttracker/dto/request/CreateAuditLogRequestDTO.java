package com.buildermaster.projecttracker.dto.request;

import com.buildermaster.projecttracker.model.EActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating audit log entries")
public class CreateAuditLogRequestDTO {

    @Schema(description = "Type of action performed", example = "CREATE")
    private EActionType actionType;

    @Schema(description = "Type of entity affected", example = "Project")
    private String entityType;

    @Schema(description = "ID of the affected entity")
    private UUID entityId;

    @Schema(description = "Name of the user who performed the action", example = "admin@example.com")
    private String actorName;

    @Schema(description = "Additional data related to the action")
    private Map<String, Object> payload;
}
