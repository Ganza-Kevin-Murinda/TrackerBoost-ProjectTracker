package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.request.CreateUserRequestDTO;
import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.JwtResponseDTO;
import com.buildermaster.projecttracker.model.EActionType;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.service.AuditService;
import com.buildermaster.projecttracker.service.UserService;
import com.buildermaster.projecttracker.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;
    private final AuditService  auditService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register new user (local)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<String>> registerUser(@Valid @RequestBody CreateUserRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        User createdUser = userService.createUser(user);

        return createdUser != null
                ? ResponseEntity.ok(ApiResponseDTO.created("User registered successfully"))
                : ResponseEntity.badRequest().body(ApiResponseDTO.created("User not found"));
    }

    @Operation(summary = "Login and receive JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<JwtResponseDTO>> loginUser(@Valid @RequestBody CreateUserRequestDTO request) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user.getUsername());

            auditService.logAction(
                    EActionType.LOGIN_SUCCESS,
                    "USER",
                    user.getId(),
                    user.getUsername(),
                    user
            );

            JwtResponseDTO response = JwtResponseDTO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

            return token != null
                    ? ResponseEntity.ok(ApiResponseDTO.success("Login successful", response))
                    : ResponseEntity.status(401).body(ApiResponseDTO.error("Invalid credentials", 401));

    }
}
