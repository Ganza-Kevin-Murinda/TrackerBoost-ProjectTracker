package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.response.AuditLogResponseDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for audit operations
 * Provides methods for logging actions and retrieving audit trails
 */
public interface AuditService {

    AuditLog logAction(EActionType actionType, String entityType, UUID entityId, String actor, Object entity);

    Page<AuditLogResponseDTO> getAuditTrail(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLogResponseDTO> getUserActions(String actorName, Pageable pageable);

    Page<AuditLogResponseDTO> getActionsByType(EActionType actionType, Pageable pageable);

    Page<AuditLogResponseDTO> getAuditsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLogResponseDTO> getAllAudits(Pageable pageable);
}
