package com.codefactory.urlshortener.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("url_mappings")
data class UrlMapping(
    @Id
    val id: Long? = null,
    val shortId: String,
    val originalUrl: String,
    val urlHash: ByteArray,
    val createdAt: Instant = Instant.now(),
)
