package com.buildermaster.projecttracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generic API response wrapper")
public class ApiResponseDTO<T> {

    @Schema(description = "Indicates if the operation was successful")
    private Boolean success;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code")
    private Integer statusCode;

    // Static factory methods for common responses
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponseDTO<T> created(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message, Integer statusCode) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .build();
    }
}
