package com.codefactory.urlshortener.api.response

/**
 * Response object containing URL mapping details.
 * Used for both creation and resolution endpoints.
 */
data class UrlResponse(
    val shortId: String,
    val originalUrl: String,
)
