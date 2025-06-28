package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.dto.request.CreateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.request.UpdateDeveloperRequestDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.exception.DuplicateResourceException;
import com.buildermaster.projecttracker.exception.ResourceNotFoundException;
import com.buildermaster.projecttracker.exception.UserNotFoundException;
import com.buildermaster.projecttracker.exception.ValidationException;
import com.buildermaster.projecttracker.model.*;
import com.buildermaster.projecttracker.repository.DeveloperRepository;
import com.buildermaster.projecttracker.repository.UserRepository;
import com.buildermaster.projecttracker.service.AuditService;
import com.buildermaster.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DeveloperService interface
 * Provides business logic for developer management operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    private static final String ENTITY_TYPE = "Developer";
    private static final String SYSTEM_ACTOR = "ADMIN";

    // ===== CRUD OPERATIONS =====

    @Override
    public DeveloperResponseDTO createDeveloperProfile(CreateDeveloperRequestDTO createRequest) {
        log.info("Creating new developer with email: {}", createRequest.getEmail());

        User user = userRepository.findByUsername(createRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User with email not found"));

        // Validate email uniqueness
        if (developerRepository.existsByEmail(user.getUsername())) {
            log.warn("Attempt to create developer with existing email: {}", createRequest.getEmail());
            throw new DuplicateResourceException(ENTITY_TYPE, "email", createRequest.getEmail());
        }

        // Create and save developer
        Developer developer = new Developer(
                createRequest.getName(),
                createRequest.getEmail(),
                createRequest.getSkills(),
                user
        );

        Developer savedDeveloper = developerRepository.save(developer);
        log.info("Successfully created developer with ID: {}", savedDeveloper.getId());

        // Log audit
        auditService.logAction(EActionType.CREATE, ENTITY_TYPE, savedDeveloper.getId(), "DEVELOPER", savedDeveloper);

        return mapToResponseDTO(savedDeveloper);
    }

    @Override
    @Cacheable(value = "developers", key = "#developerId")
    @Transactional(readOnly = true)
    public DeveloperResponseDTO getDeveloperById(UUID developerId) {
        log.debug("Retrieving developer by ID: {}", developerId);

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> {
                    log.warn("Developer not found with ID: {}", developerId);
                    return new ResourceNotFoundException(ENTITY_TYPE, developerId);
                });

        log.debug("Found developer: {}", developer.getName());
        return mapToResponseDTO(developer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperResponseDTO> getAllDevelopers(Pageable pageable) {
        log.debug("Retrieving developers with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Developer> developerPage = developerRepository.findAllByOrderByCreatedDateDesc(pageable);
        log.debug("Found {} developers in page {} of {}",
                developerPage.getNumberOfElements(),
                developerPage.getNumber() + 1,
                developerPage.getTotalPages());

        return developerPage.map(this::mapToResponseDTO);
    }

    @Override
    @CacheEvict(value = {"developers", "allDevelopers"}, allEntries = true)
    public DeveloperResponseDTO updateDeveloperProfile(UUID developerId, UpdateDeveloperRequestDTO updateRequest) {
        log.info("Updating developer with ID: {}", developerId);

        Developer existingDeveloper = developerRepository.findById(developerId)
                .orElseThrow(() -> {
                    log.warn("Developer not found for update with ID: {}", developerId);
                    return new ResourceNotFoundException(ENTITY_TYPE, developerId);
                });

        // Validate email changes
        if (!existingDeveloper.getEmail().equals(updateRequest.getEmail())) {
            if (developerRepository.existsByEmail(updateRequest.getEmail())) {
                log.warn("Attempt to update with existing email: {}", updateRequest.getEmail());
                throw new DuplicateResourceException(ENTITY_TYPE, "email", updateRequest.getEmail());
            }
        }

        // Update fields
        existingDeveloper.setName(updateRequest.getName());
        existingDeveloper.setEmail(updateRequest.getEmail());
        existingDeveloper.setSkills(updateRequest.getSkills());

        Developer updatedDeveloper = developerRepository.save(existingDeveloper);
        log.info("Successfully updated developer with ID: {}", updatedDeveloper.getId());

        // Log audit
        auditService.logAction(EActionType.UPDATE, ENTITY_TYPE, updatedDeveloper.getId(), SYSTEM_ACTOR, updatedDeveloper);

        return mapToResponseDTO(updatedDeveloper);
    }

    @Override
    @CacheEvict(value = {"developers", "allDevelopers"}, allEntries = true)
    public boolean deleteDeveloperProfile(UUID developerId) {
        log.info("Deleting developer with ID: {}", developerId);

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> {
                    log.warn("Developer not found for deletion with ID: {}", developerId);
                    return new ResourceNotFoundException(ENTITY_TYPE, developerId);
                });

        if(developer != null) {
            // Handle task reassignment if developer has tasks
            if (!developer.getTasks().isEmpty()) {
                log.info("Developer has {} assigned tasks. Removing task associations.", developer.getTasks().size());
                // Clear the bidirectional relationship
                for (Task task : developer.getTasks()) {
                    task.setDeveloper(null);
                }
                developer.getTasks().clear();
            }

            // Log audit before deletion
            auditService.logAction(EActionType.DELETE, ENTITY_TYPE, developerId, SYSTEM_ACTOR, developer);

            developerRepository.delete(developer);
            log.info("Successfully deleted developer with ID: {}", developerId);
            return true;
        }

        return false;
    }

    // ===== BUSINESS OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponseDTO getDeveloperByEmail(String email) {
        log.debug("Retrieving developer by email: {}", email);

        if (!StringUtils.hasText(email)) {
            throw new ValidationException("email", "Email cannot be empty");
        }

        Developer developer = developerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Developer not found with email: {}", email);
                    return new ResourceNotFoundException(ENTITY_TYPE, "email", email);
                });

        log.debug("Found Developer: {}", developer.getName());
        return mapToResponseDTO(developer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperSummaryDTO> getTop5DevelopersByTaskCount() {
        log.debug("Retrieving top 5 developers by task count");

        Pageable top5 = PageRequest.of(0, 5);
        Page<Developer> topDevelopers = developerRepository.findTop5DevelopersByTaskCount(top5);

        log.debug("Found {} Top Developers", topDevelopers.getNumberOfElements());

        return topDevelopers.getContent().stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperSummaryDTO> getDevelopersWithoutTasks() {
        log.debug("Retrieving developers without tasks");

        List<Developer> developersWithoutTasks = developerRepository.findDevelopersWithoutTasks();
        log.debug("Found {} developers without tasks", developersWithoutTasks.size());

        return developersWithoutTasks.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperSummaryDTO> getDevelopersByTaskStatus(ETaskStatus taskStatus) {
        log.debug("Retrieving developers by task status: {}", taskStatus);

        if (taskStatus == null) {
            throw new ValidationException("taskStatus", "Task status cannot be null");
        }

        List<Developer> developers = developerRepository.findDevelopersByTaskStatus(taskStatus);
        log.debug("Found {} developers with tasks in status: {}", developers.size(), taskStatus);

        return developers.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperResponseDTO> searchDevelopersByName(String name, Pageable pageable) {
        log.debug("Searching developers by name: {} with pagination", name);

        if (!StringUtils.hasText(name)) {
            throw new ValidationException("name", "Search name cannot be empty");
        }

        Page<Developer> developerPage = developerRepository.findByNameContainingIgnoreCase(name, pageable);
        log.debug("Found {} developers matching name search: {}",
                developerPage.getTotalElements(), name);

        return developerPage.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeveloperResponseDTO> searchDevelopersBySkill(String skill, Pageable pageable) {
        log.debug("Searching developers by skill: {} with pagination", skill);

        if (!StringUtils.hasText(skill)) {
            throw new ValidationException("skill", "Search skill cannot be empty");
        }

        Page<Developer> developerPage = developerRepository.findBySkillsContainingIgnoreCase(skill, pageable);
        log.debug("Found {} developers matching skill search: {}",
                developerPage.getTotalElements(), skill);

        return developerPage.map(this::mapToResponseDTO);
    }

    // ===== UTILITY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public Long getTotalDeveloperCount() {
        log.debug("Getting total developer count");

        long count = developerRepository.count();
        log.debug("Total developer count: {}", count);

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDeveloperCountWithTasks() {
        log.debug("Getting count of developers with tasks");

        Long count = developerRepository.countDevelopersWithTasks();
        log.debug("Developers with tasks count: {}", count);

        return count;
    }

    // ===== PRIVATE HELPER METHODS =====

    private DeveloperResponseDTO mapToResponseDTO(Developer developer) {
        return DeveloperResponseDTO.builder()
                .id(developer.getId())
                .name(developer.getName())
                .email(developer.getEmail())
                .skills(developer.getSkills())
                .totalTaskCount(developer.getTasks() != null ? developer.getTasks().size() : 0)
                .createdDate(developer.getCreatedDate())
                .updatedDate(developer.getUpdatedDate())
                .build();
    }

    private DeveloperSummaryDTO mapToSummaryDTO(Developer developer) {
        // Truncate skills for summary (first 100 characters)
        String primarySkills = developer.getSkills();
        if (primarySkills != null && primarySkills.length() > 100) {
            primarySkills = primarySkills.substring(0, 100) + "...";
        }

        return DeveloperSummaryDTO.builder()
                .id(developer.getId())
                .name(developer.getName())
                .email(developer.getEmail())
                .primarySkills(primarySkills)
                .totalTaskCount(developer.getTasks() != null ? developer.getTasks().size() : 0)
                .build();
    }
}
