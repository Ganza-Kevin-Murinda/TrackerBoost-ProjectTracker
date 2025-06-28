package com.buildermaster.projecttracker.repository;

import com.buildermaster.projecttracker.model.Developer;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Developer entity
 * Provides CRUD operations and custom query methods for Developer management
 */
@Repository
public interface DeveloperRepository extends JpaRepository<Developer, UUID> {

    Optional<Developer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT d FROM Developer d LEFT JOIN d.tasks t GROUP BY d ORDER BY COUNT(t) DESC")
    Page<Developer> findTop5DevelopersByTaskCount(Pageable pageable);

    @Query("SELECT d FROM Developer d WHERE d.tasks IS EMPTY")
    List<Developer> findDevelopersWithoutTasks();

    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.status = :status")
    List<Developer> findDevelopersByTaskStatus(@Param("status") ETaskStatus status);

    @Query("SELECT COUNT(DISTINCT d) FROM Developer d JOIN d.tasks t")
    Long countDevelopersWithTasks();

    Page<Developer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Developer> findBySkillsContainingIgnoreCase(String skill, Pageable pageable);

    Page<Developer> findAllByOrderByCreatedDateDesc(Pageable pageable);

}