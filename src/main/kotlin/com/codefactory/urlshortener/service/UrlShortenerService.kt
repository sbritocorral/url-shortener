package com.codefactory.urlshortener.service

import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.domain.ShortenedUrl
import com.codefactory.urlshortener.entity.UrlMapping
import com.codefactory.urlshortener.entity.toDomain
import com.codefactory.urlshortener.exception.UrlNotFoundException
import com.codefactory.urlshortener.repository.UrlMappingRepository
import com.codefactory.urlshortener.util.ShortIdGenerator
import java.net.IDN
import java.net.URI
import java.security.MessageDigest
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.web.util.InvalidUrlException
import reactor.core.publisher.Mono

@Service
class UrlShortenerService(
    private val repository: UrlMappingRepository
) {
    private val logger = LoggerFactory.getLogger(UrlShortenerService::class.java)

    fun createShortUrl(request: CreateUrlRequest): Mono<ShortenedUrl> {
        logger.debug("Creating short URL for: {}", request.originalUrl)
        val normalizedUrl = normalizeUrl(request.originalUrl)

        val urlHash =
            MessageDigest.getInstance("SHA-256")
                .digest(normalizedUrl.toByteArray())

        return repository.findByUrlHash(urlHash)
            .flatMap { existing ->
                logger.debug("Found existing shortId: {}", existing.shortId)
                Mono.just(existing.toDomain())
            }.switchIfEmpty(
                Mono.defer {
                    val shortId = ShortIdGenerator.generateShortId(normalizedUrl)
                    val entity =
                        UrlMapping(
                            shortId = shortId,
                            originalUrl = request.originalUrl,
                            normalizedUrl = normalizedUrl,
                            urlHash = urlHash,
                            createdAt = Instant.now(),
                        )
                    repository.save(entity)
                        .doOnSuccess { logger.debug("Created short URL: ${it.shortId}") }
                        .map { it.toDomain() }
                        .onErrorResume(DuplicateKeyException::class.java) { _ ->
                            logger.warn("Collision detected. Retrying with a random salt.")
                            val saltedShortId = ShortIdGenerator.generateShortId(request.originalUrl + System.nanoTime())
                            val secondEntity = entity.copy(shortId = saltedShortId)
                            repository.save(secondEntity)
                                .map { it.toDomain() }
                        }
                },
            )


    }

    fun resolveShortUrl(shortId: String): Mono<ShortenedUrl> {
        return repository.findByShortId(shortId)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.error(UrlNotFoundException(shortId)))
    }

    private fun normalizeUrl(url: String): String {
        return try {
            val uri = URI(url.trim())
            val normalizedUri = URI(
                uri.scheme?.lowercase() ?: "https",
                uri.userInfo,
                uri.host?.lowercase(),
                uri.port,
                uri.path?.removeSuffix("/"),
                uri.query?.split('&')?.sorted()?.joinToString("&"), // Sort query parameters
                uri.fragment
            ).normalize()

            IDN.toASCII(normalizedUri.toString()) // Handle international domains
        } catch (e: Exception) {
            throw InvalidUrlException("Unable to normalize URL: ${e.message}", e)
        }
    }
}
