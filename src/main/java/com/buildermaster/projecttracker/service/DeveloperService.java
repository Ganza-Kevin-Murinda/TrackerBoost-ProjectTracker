package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.request.CreateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for developer business operations
 * Provides methods for managing developers and their associated data
 */
public interface DeveloperService {

    // ===== CRUD OPERATIONS =====

    DeveloperResponseDTO createDeveloperProfile(CreateDeveloperRequestDTO createRequest);

    DeveloperResponseDTO getDeveloperById(UUID developerId);

    List<DeveloperResponseDTO> getAllDevelopers();

    Page<DeveloperResponseDTO> getAllDevelopers(Pageable pageable);

    DeveloperResponseDTO updateDeveloperProfile(UUID developerId, UpdateDeveloperRequestDTO updateRequest);

    boolean deleteDeveloperProfile(UUID developerId);

    // ===== BUSINESS OPERATIONS =====

    DeveloperResponseDTO getDeveloperByEmail(String email);

    List<DeveloperSummaryDTO> getTop5DevelopersByTaskCount();

    List<DeveloperSummaryDTO> getDevelopersWithoutTasks();

    List<DeveloperSummaryDTO> getDevelopersByTaskStatus(ETaskStatus taskStatus);

    Page<DeveloperResponseDTO> searchDevelopersByName(String name, Pageable pageable);

    Page<DeveloperResponseDTO> searchDevelopersBySkill(String skill, Pageable pageable);

    // ===== UTILITY METHODS =====

    boolean existsByEmail(String email);

    Long getTotalDeveloperCount();

    Long getDeveloperCountWithTasks();
}