package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.EAuthProvider;
import com.buildermaster.projecttracker.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing user information")
public class UserResponseDTO {

    @Schema(description = "Unique user identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Username's address", example = "john.doe@example.com")
    private String username;

    @Schema(description = "Developer's role", example = "ROLE_DEVELOPER")
    private ERole role;

    @Schema(description = "Developer's authorization process", example = "LOCAL")
    private EAuthProvider authProvider;

    @Schema(description = "User creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "User last update timestamp")
    private LocalDateTime updatedDate;
}

