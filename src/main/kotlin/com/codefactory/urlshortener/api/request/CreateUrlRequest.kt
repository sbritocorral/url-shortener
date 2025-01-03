package com.codefactory.urlshortener.api.request

import com.codefactory.urlshortener.validation.ValidUrl
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request object for URL shortening.
 * Includes validation for URL format and length.
 */
@Schema(description = "Request for creating a shortened URL")
data class CreateUrlRequest(
    @Schema(
        description = "The original URL to be shortened",
        example = "https://example.com/very/long/url",
        required = true,
        maxLength = 2048,
    )
    @field:NotBlank(message = "URL cannot be empty")
    @field:ValidUrl
    @field:Size(max = 2048, message = "URL is too long")
    val originalUrl: String,
)
