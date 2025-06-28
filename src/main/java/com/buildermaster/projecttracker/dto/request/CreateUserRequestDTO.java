package com.buildermaster.projecttracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new user")
public class CreateUserRequestDTO {

    @NotBlank(message = "Username is required")
    @Email(message = "Username must be valid")
    @Schema(description = "Username's address", example = "john.doe@example.com")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password address")
    private String password;
}

