package com.buildermaster.projecttracker.model.audit;

import com.buildermaster.projecttracker.model.EActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AuditLog MongoDB document for tracking entity changes
 */
@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    private EActionType actionType;

    private String entityType;

    private UUID entityId;

    private LocalDateTime timestamp;

    private String actorName;

    private Map<String, Object> payload;

    // Custom constructor without ID (MongoDB will auto-generate)
    public AuditLog(EActionType actionType, String entityType, UUID entityId, String actorName, Map<String, Object> payload) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actorName = actorName;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }
}
