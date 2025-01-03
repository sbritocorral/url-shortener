package com.codefactory.urlshortener.api.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response object containing URL mapping details.
 * Used for both creation and resolution endpoints.
 */
@Schema(
    description = "Response containing the shortened URL details",
)
data class UrlResponse(
    @Schema(
        description = "The generated short ID for the URL",
        example = "Ab12Cd3E",
        pattern = "[A-Za-z0-9]{8}",
    )
    val shortId: String,
    @Schema(
        description = "The original, unmodified URL",
        example = "https://example.com/very/long/url",
    )
    val originalUrl: String,
)
