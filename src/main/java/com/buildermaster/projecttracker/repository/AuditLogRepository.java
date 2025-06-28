package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.audit.AuditLog;
import com.buildermaster.projecttracker.model.EActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog MongoDB document
 * Provides CRUD operations and custom query methods for audit log management
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByActorNameOrderByTimestampDesc(String actorName, Pageable pageable);

    List<AuditLog> findByActionType(EActionType actionType);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Long countByEntityType(String entityType);

    Long countByActionType(EActionType actionType);

    Long countByActorName(String actorName);

    Long deleteByTimestampBefore(LocalDateTime timestamp);
}