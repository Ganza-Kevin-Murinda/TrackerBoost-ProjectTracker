package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.dto.response.UserResponseDTO;
import com.buildermaster.projecttracker.dto.response.UserSummaryDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.EAuthProvider;
import com.buildermaster.projecttracker.model.ERole;
import com.buildermaster.projecttracker.exception.DuplicateResourceException;
import com.buildermaster.projecttracker.exception.UserNotFoundException;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.repository.UserRepository;
import com.buildermaster.projecttracker.service.AuditService;
import com.buildermaster.projecttracker.service.UserService;
import com.buildermaster.projecttracker.security.oauth2.OAuth2UserInfo;
import com.buildermaster.projecttracker.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final JwtUtil jwtUtil;

    private static final String ENTITY_TYPE = "User";

    @Override
    public User createUser(User user) {
        log.info("Registering new local user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("User with email already exists: " + user.getUsername());
        }

        // Set defaults
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(EAuthProvider.LOCAL);
        user.setRole(ERole.ROLE_DEVELOPER); // assign developer role by default

        User savedUser = userRepository.save(user);
        log.info("Successfully registered local user with ID: {}", savedUser.getId());

        // Add audit log
        auditService.logAction(EActionType.CREATE, ENTITY_TYPE, savedUser.getId(), "SYSTEM", savedUser);

        return savedUser;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAllUserDetails()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Used during OAuth2 callback to register or return existing user.
     */
    @Override
    public User registerOAuth2User(OAuth2UserInfo info) {
        log.info("Google OAuth2 login attempt for email: {}", info.getEmail());

        return userRepository.findByUsername(info.getEmail())
                .orElseGet(() -> {
                    log.info(">>> User not found, creating new one...");
            User newUser = new User();
            newUser.setUsername(info.getEmail());
            newUser.setPassword("");
            newUser.setAuthProvider(EAuthProvider.GOOGLE);
            newUser.setRole(ERole.ROLE_CONTRACTOR); // Default role for OAuth2 users

            User savedUser = userRepository.save(newUser);
            log.info("New Google OAuth2 user created: {}", savedUser.getUsername());

            // Add audit log
            auditService.logAction(EActionType.CREATE, ENTITY_TYPE, savedUser.getId(), "SYSTEM", savedUser);
            return savedUser;
        });
    }

    /**
     * Retrieve current authenticated user.
     */
    @Override
    public UserSummaryDTO getCurrentUserDetails() {
        log.info("Getting user's details");
        return mapToSummaryDTO(getCurrentUser());
    }

    public User getCurrentUser() {
        String email = jwtUtil.getCurrentUserEmail()
                .orElseThrow(() -> new UserNotFoundException("No authenticated user found"));

        return userRepository.findByUsername(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public void assignRoleToUser(UUID userId, ERole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Assigned role {} to user {}", role, user.getUsername());
        auditService.logAction(EActionType.UPDATE, ENTITY_TYPE, updatedUser.getId(), "ADMIN", updatedUser);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .build();
    }

    private UserSummaryDTO mapToSummaryDTO(User user) {
        return UserSummaryDTO.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("User with username " + username + " not found"));
    }
}

