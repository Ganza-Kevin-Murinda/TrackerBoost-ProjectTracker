package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.dto.response.AuditLogResponseDTO;
import com.buildermaster.projecttracker.exception.AuditException;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.audit.AuditLog;
import com.buildermaster.projecttracker.repository.AuditLogRepository;
import com.buildermaster.projecttracker.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuditService for audit operations
 * Provides asynchronous audit logging and comprehensive audit trail retrieval
 */
@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public AuditServiceImpl(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuditLog logAction(EActionType actionType, String entityType, UUID entityId, String actor, Object entity) {
        try {
            Map<String, Object> payload = createAuditPayload(entity);
            AuditLog auditLog = new AuditLog(actionType, entityType, entityId, actor, payload);

            AuditLog savedLog = auditLogRepository.save(auditLog);
            log.info("Audit log created: {} action on {} entity {} by {}",
                    actionType, entityType, entityId, actor);
            return savedLog;
        } catch (Exception e) {
            log.error("Failed to create audit log for {} action on {} entity {} by {}: {}",
                    actionType, entityType, entityId, actor, e.getMessage(), e);
            throw new AuditException("Failed to create audit log", e);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> getAuditTrail(String entityType, UUID entityId, Pageable pageable) {
        try {
            Page<AuditLog> auditLogsPage = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
            List<AuditLogResponseDTO> responseDTOs = auditLogsPage.getContent().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return new PageImpl<>(responseDTOs, pageable, auditLogsPage.getTotalElements());
        } catch (Exception e) {
            log.error("Failed to retrieve paginated audit trail for {} entity {}: {}",
                    entityType, entityId, e.getMessage(), e);
            throw new AuditException("Failed to retrieve paginated audit trail", e);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> getUserActions(String actorName, Pageable pageable) {
        try {
            Page<AuditLog> auditLogsPage = auditLogRepository.findByActorNameOrderByTimestampDesc(actorName, pageable);
            List<AuditLogResponseDTO> responseDTOs = auditLogsPage.getContent().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return new PageImpl<>(responseDTOs, pageable, auditLogsPage.getTotalElements());
        } catch (Exception e) {
            log.error("Failed to retrieve user actions for {}: {}", actorName, e.getMessage(), e);
            throw new AuditException("Failed to retrieve user actions", e);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> getActionsByType(EActionType actionType, Pageable pageable) {
        try {
            List<AuditLog> allActionLogs = auditLogRepository.findByActionType(actionType);

            return getAuditLog(pageable, allActionLogs);
        } catch (Exception e) {
            log.error("Failed to retrieve actions by type {}: {}", actionType, e.getMessage(), e);
            throw new AuditException("Failed to retrieve actions by type", e);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> getAuditsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        try {
            List<AuditLog> allDateRangeLogs = auditLogRepository.findByTimestampBetween(start, end);

            return getAuditLog(pageable, allDateRangeLogs);
        } catch (Exception e) {
            log.error("Failed to retrieve audits by date range {} to {}: {}",
                    start, end, e.getMessage(), e);
            throw new AuditException("Failed to retrieve audits by date range", e);
        }
    }

    private Page<AuditLogResponseDTO> getAuditLog(Pageable pageable, List<AuditLog> allDateRangeLogs) {
        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min((startIdx + pageable.getPageSize()), allDateRangeLogs.size());
        List<AuditLog> pageContent = allDateRangeLogs.subList(startIdx, endIdx);

        List<AuditLogResponseDTO> responseDTOs = pageContent.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(responseDTOs, pageable, allDateRangeLogs.size());
    }

    @Override
    public Page<AuditLogResponseDTO> getAllAudits(Pageable pageable) {
        try {
            Page<AuditLog> auditLogsPage = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
            List<AuditLogResponseDTO> responseDTOs = auditLogsPage.getContent().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return new PageImpl<>(responseDTOs, pageable, auditLogsPage.getTotalElements());
        } catch (Exception e) {
            log.error("Failed to retrieve all audits: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve all audits", e);
        }
    }

    private Map<String, Object> createAuditPayload(Object entity) {
        if (entity == null) {
            return new HashMap<>();
        }

        try {
            // Convert entity to Map using ObjectMapper
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.convertValue(entity, Map.class);

            // Add metadata
            payload.put("_entityClass", entity.getClass().getSimpleName());
            payload.put("_captureTime", LocalDateTime.now());

            return payload;
        } catch (Exception e) {
            log.warn("Failed to serialize entity {} to payload: {}",
                    entity.getClass().getSimpleName(), e.getMessage());

            // Fallback to basic info
            Map<String, Object> fallbackPayload = new HashMap<>();
            fallbackPayload.put("_entityClass", entity.getClass().getSimpleName());
            fallbackPayload.put("_error", "Failed to serialize entity");
            fallbackPayload.put("_captureTime", LocalDateTime.now());
            return fallbackPayload;
        }
    }

    /**
     * Convert AuditLog entity to AuditLogResponseDTO
     * @param auditLog the audit log entity
     * @return response DTO with formatted data
     */
    private AuditLogResponseDTO convertToResponseDTO(AuditLog auditLog) {
        return AuditLogResponseDTO.builder()
                .id(auditLog.getId())
                .actionType(auditLog.getActionType())
                .actionDescription(generateActionDescription(auditLog))
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .timestamp(auditLog.getTimestamp())
                .formattedTimestamp(auditLog.getTimestamp().format(DISPLAY_FORMATTER))
                .actorName(auditLog.getActorName())
                .payload(auditLog.getPayload())
                .changesSummary(generateChangesSummary(auditLog))
                .build();
    }

    /**
     * Generate human-readable action description
     * @param auditLog the audit log
     * @return formatted action description
     */
    private String generateActionDescription(AuditLog auditLog) {
        String entity = auditLog.getEntityType().toLowerCase();

        return switch (auditLog.getActionType()) {
            case CREATE -> String.format("Created a new %s", entity);
            case UPDATE -> String.format("Updated %s", entity);
            case DELETE -> String.format("Deleted %s", entity);
            case LOGIN_SUCCESS -> String.format("Successfully logged in %s", entity);
        };
    }

    /**
     * Generate changes summary from payload
     * @param auditLog the audit log
     * @return summary of changes made
     */
    private String generateChangesSummary(AuditLog auditLog) {
        if (auditLog.getPayload() == null || auditLog.getPayload().isEmpty()) {
            return "No changes recorded";
        }

        try {
            Map<String, Object> payload = auditLog.getPayload();
            StringBuilder summary = new StringBuilder();

            // Look for common fields that indicate changes
            if (payload.containsKey("name")) {
                summary.append("Name: ").append(payload.get("name")).append("; ");
            }
            if (payload.containsKey("status")) {
                summary.append("Status: ").append(payload.get("status")).append("; ");
            }
            if (payload.containsKey("description")) {
                String desc = payload.get("description").toString();
                if (desc.length() > 50) {
                    desc = desc.substring(0, 50) + "...";
                }
                summary.append("Description: ").append(desc).append("; ");
            }

            // If no specific fields found, show generic info
            if (summary.isEmpty()) {
                summary.append("Entity modified with ")
                        .append(payload.size())
                        .append(" field(s) changed");
            }

            return summary.toString().replaceAll("; $", "");
        } catch (Exception e) {
            log.warn("Failed to generate changes summary for audit log {}: {}",
                    auditLog.getId(), e.getMessage());
            return "Changes summary unavailable";
        }
    }
}