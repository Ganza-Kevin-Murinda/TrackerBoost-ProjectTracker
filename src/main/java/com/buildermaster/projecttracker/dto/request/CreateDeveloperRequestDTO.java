package com.buildermaster.projecttracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new developer")
public class CreateDeveloperRequestDTO {

    @NotBlank(message = "Developer name is required")
    @Size(min = 2, max = 100, message = "Developer name must be between 2 and 100 characters")
    @Schema(description = "Full name of the developer", example = "John Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Developer's email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Skills are required")
    @Size(max = 500, message = "Skills cannot exceed 500 characters")
    @Schema(description = "Developer's technical skills", example = "Java, Spring Boot, React, PostgreSQL")
    private String skills;
}