package com.codefactory.urlshortener.service

import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.entity.UrlMapping
import com.codefactory.urlshortener.entity.toDomain
import com.codefactory.urlshortener.exception.UrlNotFoundException
import com.codefactory.urlshortener.repository.UrlMappingRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.util.InvalidUrlException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UrlShortenerServiceTest {
    private lateinit var repository: UrlMappingRepository
    private lateinit var service: UrlShortenerService

    @BeforeEach
    fun setup() {
        repository = mockk()
        service = UrlShortenerService(repository)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    @Nested
    inner class CreateShortUrl {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "http://example.com:8080",
                "http://subdomain.example.com",
                "http://example.com/path?param=value",
                "http://example.com#fragment"
            ]
        )
        fun `should handle URLs with different components`(url: String) {
            // Arrange
            val request = CreateUrlRequest(url)
            val urlMappingSlot = slot<UrlMapping>()

            coEvery { repository.findByUrlHash(any()) } returns Mono.empty()
            coEvery { repository.save(capture(urlMappingSlot)) } answers {
                Mono.just(firstArg())
            }

            // Act & Assert
            StepVerifier.create(service.createShortUrl(request))
                .assertNext { shortened ->
                    // Don't check exact URL, just verify it's properly formatted
                    assertTrue(shortened.originalUrl.startsWith("http://"))
                    assertTrue(shortened.normalizedUrl.startsWith("https://"))
                    assertTrue(shortened.shortId.length == 8)
                }
                .verifyComplete()

            coVerify(exactly = 1) {
                repository.findByUrlHash(any())
                repository.save(any())
            }
        }

        @Test
        fun `should handle multiple collision retries`() {
            // Arrange
            val request = CreateUrlRequest("https://example.com")
            val finalMapping = UrlMapping(
                id = 1L,
                shortId = "finalId",
                originalUrl = "https://example.com",
                normalizedUrl = "https://example.com",
                urlHash = ByteArray(32),
                createdAt = Instant.now()
            )

            coEvery { repository.findByUrlHash(any()) } returns Mono.empty()
            coEvery { repository.save(any()) } returnsMany listOf(
                Mono.error(DuplicateKeyException("Duplicate key")),
                Mono.just(finalMapping)
            )

            // Act & Assert
            StepVerifier.create(service.createShortUrl(request))
                .expectNext(finalMapping.toDomain())
                .verifyComplete()

            coVerify(exactly = 1) { repository.findByUrlHash(any()) }
            coVerify(exactly = 2) { repository.save(any()) }
        }
    }

    @Nested
    inner class ResolveShortUrl {
        @Test
        fun `should return URL for valid short ID`() {
            // Arrange
            val shortId = "abc123"
            val mapping = UrlMapping(
                id = 1L,
                shortId = shortId,
                originalUrl = "https://example.com",
                normalizedUrl = "https://example.com",
                urlHash = ByteArray(32),
                createdAt = Instant.now()
            )

            coEvery { repository.findByShortId(shortId) } returns Mono.just(mapping)

            // Act & Assert
            StepVerifier.create(service.resolveShortUrl(shortId))
                .assertNext { shortened ->
                    assertEquals(mapping.shortId, shortened.shortId)
                    assertEquals(mapping.originalUrl, shortened.originalUrl)
                }
                .verifyComplete()

            coVerify(exactly = 1) { repository.findByShortId(shortId) }
        }

        @Test
        fun `should throw UrlNotFoundException for non-existent short ID`() {
            // Arrange
            val shortId = "nonexistent"

            coEvery { repository.findByShortId(shortId) } returns Mono.empty()

            // Act & Assert
            StepVerifier.create(service.resolveShortUrl(shortId))
                .expectError(UrlNotFoundException::class.java)
                .verify()

            coVerify(exactly = 1) { repository.findByShortId(shortId) }
        }
    }
}

