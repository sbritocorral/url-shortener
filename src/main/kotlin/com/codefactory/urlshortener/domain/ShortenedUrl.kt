package com.codefactory.urlshortener.domain

import com.codefactory.urlshortener.api.response.UrlResponse
import java.time.Instant

/**
 * Domain model representing a shortened URL.
 * Contains all URL mapping details including metadata.
 */
data class ShortenedUrl(
    val shortId: String,
    val originalUrl: String,
    val normalizedUrl: String,
    val createdAt: Instant,
)

fun ShortenedUrl.toResponse() =
    UrlResponse(
        shortId = shortId,
        originalUrl = originalUrl,
    )
