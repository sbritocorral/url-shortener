package com.codefactory.urlshortener.repository

import com.codefactory.urlshortener.entity.UrlMapping
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UrlMappingRepository : ReactiveCrudRepository<UrlMapping, Long> {
    fun findByShortId(shortId: String): Mono<UrlMapping>

    fun findByUrlHash(hash: ByteArray): Mono<UrlMapping>
}
