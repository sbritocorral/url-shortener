package com.codefactory.urlshortener.api.response

import java.time.Instant

/**
 * Standardized error response format.
 * Used across all error scenarios in the application.
 */
data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)
