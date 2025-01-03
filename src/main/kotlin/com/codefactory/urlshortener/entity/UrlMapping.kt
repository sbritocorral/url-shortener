package com.codefactory.urlshortener.entity

import com.codefactory.urlshortener.domain.ShortenedUrl
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

/**
 * Database representation of a shortened URL. Stores the original URL along with its
 * shortened version and metadata. Includes hash-based duplicate detection and
 * timestamp tracking for each entry.
 */
@Suppress("ArrayInDataClass")
@Table("url_mappings")
data class UrlMapping(
    @Id
    val id: Long? = null,
    val shortId: String,
    val originalUrl: String,
    val normalizedUrl: String,
    val urlHash: ByteArray,
    val createdAt: Instant = Instant.now(),
)

fun UrlMapping.toDomain() =
    ShortenedUrl(
        shortId = shortId,
        originalUrl = originalUrl,
        normalizedUrl = normalizedUrl,
        createdAt = createdAt,
    )
