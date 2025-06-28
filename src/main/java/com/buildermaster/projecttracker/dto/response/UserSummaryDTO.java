package com.buildermaster.projecttracker.dto.response;

import com.buildermaster.projecttracker.model.EAuthProvider;
import com.buildermaster.projecttracker.model.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary DTO for user details")
public class UserSummaryDTO {

    @Schema(description = "Username's address", example = "john.doe@example.com")
    private String username;

    @Schema(description = "Developer's role", example = "ROLE_DEVELOPER")
    private ERole role;

    @Schema(description = "Developer's authorization process", example = "LOCAL")
    private EAuthProvider authProvider;
}
