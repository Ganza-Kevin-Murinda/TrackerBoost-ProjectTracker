package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO containing the JWT and user info")
public class JwtResponseDTO {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String token;

    @Schema(description = "Token type (usually 'Bearer')", example = "Bearer")
    private String tokenType;

    @Schema(description = "Logged-in user's username/email", example = "john.doe@example.com")
    private String username;

    @Schema(description = "User's role", example = "ROLE_DEVELOPER")
    private ERole role;
}
