package org.digilib.library.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Standard error response body")
public record ErrorResponse(
        @Schema(description = "Application-specific error code", example = "1")
        Integer code,

        @Schema(description = "HTTP status code", example = "400")
        Integer status,

        @Schema(description = "HTTP reason phrase", example = "BAD_REQUEST")
        String error,

        @Schema(description = "Human readable message", example = "Something went wrong")
        String message,

        @Schema(description = "Request path", example = "/api/books")
        String path,

        @Schema(description = "Extra fields depending on error type")
        Map<String, Object> details,

        @Schema(description = "Time when the error occurred")
        OffsetDateTime timestamp
) {}