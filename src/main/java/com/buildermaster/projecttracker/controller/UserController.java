package com.buildermaster.projecttracker.controller;

import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.UserResponseDTO;
import com.buildermaster.projecttracker.dto.response.UserSummaryDTO;
import com.buildermaster.projecttracker.model.ERole;
import com.buildermaster.projecttracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user information and roles")
public class UserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users. Requires ADMIN role.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Users retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))
                    )

            }
    )
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return users != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Fetched all users", users))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("Developer not found", 404));
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current authenticated user",
            description = "Returns details about the currently logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseDTO.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<UserSummaryDTO>> getCurrentUser() {
        UserSummaryDTO currentUser = userService.getCurrentUserDetails();
        return currentUser != null
                ? ResponseEntity.ok(ApiResponseDTO.success("Current user retrieved", currentUser))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.error("User not found", 404));
    }

    @PatchMapping("/admin/user/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Assign role to a user",
            description = "Assigns a new role to the user identified by ID. Admin access required.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role assigned successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseDTO.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Void>> assignRoleToUser(
            @Parameter(description = "UUID of the user", required = true)
            @RequestParam UUID userId,

            @Parameter(description = "New role to assign", required = true, example = "ROLE_MANAGER")
            @RequestParam ERole role) {

        userService.assignRoleToUser(userId, role);
        return ResponseEntity.ok(ApiResponseDTO.success("Role assigned to user", null));
    }
}

