package com.buildermaster.projecttracker.dto.request;

import com.buildermaster.projecttracker.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for Assigning a role to user")
public class AssignRoleRequestDTO {

    @NotNull(message = "Role is required")
    @Schema(description = "User's Role", example = "ROLE_MANAGER")
    private ERole role;
}

