package com.buildermaster.projecttracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for ProjectTracker API
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI projectTrackerOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development server");

        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Contact contact = new Contact();
        contact.setEmail("support@buildmaster.com");
        contact.setName("BuildMaster Support Team");
        contact.setUrl("https://www.buildmaster.com");

        Info info = new Info()
                .title("ProjectTracker API")
                .version("1.0.0")
                .contact(contact)
                .description("A comprehensive REST API for project tracking and management. " +
                        "This API provides full CRUD operations for project management, task tracking, " +
                        "team collaboration, progress monitoring, and detailed reporting capabilities.");
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", jwtScheme)
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}