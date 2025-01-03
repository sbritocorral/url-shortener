package com.codefactory.urlshortener.api.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Standardized error response format.
 * Used across all error scenarios in the application.
 */
@Schema(
    description = "Standard error response format",
)
data class ErrorResponse(
    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-02-20T15:30:00.123Z",
    )
    val timestamp: Instant = Instant.now(),
    @Schema(
        description = "HTTP status code",
        example = "400",
    )
    val status: Int,
    @Schema(
        description = "Error type or category",
        example = "Bad Request",
    )
    val error: String,
    @Schema(
        description = "Detailed error message",
        example = "Invalid URL format",
    )
    val message: String,
    @Schema(
        description = "The API path that generated the error",
        example = "/api/v1/urls",
    )
    val path: String,
)
