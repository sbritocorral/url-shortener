package com.codefactory.urlshortener

import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.api.response.ErrorResponse
import com.codefactory.urlshortener.api.response.UrlResponse
import com.codefactory.urlshortener.repository.UrlMappingRepository
import io.mockk.clearAllMocks
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("HttpUrlsUsage")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var repository: UrlMappingRepository

    @BeforeEach
    fun setup() {
        // Clean database before each test
        StepVerifier.create(repository.deleteAll()).verifyComplete()

        // Reset mock behavior
        clearAllMocks()
    }

    @Nested
    inner class CreateUrl {
        @Test
        fun `should create and return shortened URL`() {
            // Arrange
            val request = CreateUrlRequest("https://example.com")

            // Act & Assert
            val response =
                webTestClient.post()
                    .uri("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<UrlResponse>()
                    .returnResult()
                    .responseBody!!

            // Verify response
            assertEquals("https://example.com", response.originalUrl)
            assertNotNull(response.shortId)
            assertTrue(response.shortId.length == 8)

            // Verify persistence
            StepVerifier.create(repository.findByShortId(response.shortId))
                .assertNext { entity ->
                    assertEquals(response.originalUrl, entity.originalUrl)
                    assertEquals(response.shortId, entity.shortId)
                }
                .verifyComplete()
        }

        @Test
        fun `should return same shortId for duplicate URL`() {
            // Arrange
            val request = CreateUrlRequest("https://example.com")

            // Act - First creation
            val response1 =
                webTestClient.post()
                    .uri("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<UrlResponse>()
                    .returnResult()
                    .responseBody!!

            // Act - Second creation
            val response2 =
                webTestClient.post()
                    .uri("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<UrlResponse>()
                    .returnResult()
                    .responseBody!!

            // Assert
            assertEquals(response1.shortId, response2.shortId)
        }
    }

    @Nested
    inner class ResolveUrl {
        @Test
        fun `should resolve existing short URL`() {
            // Arrange - Create a URL first
            val createRequest = CreateUrlRequest("https://example.com")
            val createResponse =
                webTestClient.post()
                    .uri("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createRequest)
                    .exchange()
                    .expectBody<UrlResponse>()
                    .returnResult()
                    .responseBody!!

            // Act & Assert - Resolve the URL
            webTestClient.get()
                .uri("/api/v1/urls/${createResponse.shortId}")
                .exchange()
                .expectStatus().isOk
                .expectBody<UrlResponse>()
                .value { response ->
                    assertEquals(createResponse.originalUrl, response.originalUrl)
                    assertEquals(createResponse.shortId, response.shortId)
                }
        }
    }

    @Nested
    inner class UrlValidation {
        @ParameterizedTest(name = "should accept valid URL: {0}")
        @ValueSource(
            strings = [
                "http://example.com",
                "https://example.com",
                "http://subdomain.example.com",
                "https://example.com/path",
                "https://example.com/path/subpath",
                "https://example.com:8080",
                "https://example.com/path?param=value",
                "https://example.com/path#fragment",
                "https://example.com/path?param=value#fragment",
                "https://user:pass@example.com",
                "https://example.com/path/with/multiple/segments",
                "https://example.com/path.with.dots",
                "http://localhost",
                "http://localhost:8080",
                "https://123.123.123.123",
                "https://example.com/path?param1=value1&param2=value2",
            ],
        )
        fun `should accept valid URLs`(validUrl: String) {
            val request = CreateUrlRequest(validUrl)

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody<UrlResponse>()
                .value { response ->
                    assertEquals(validUrl, response.originalUrl)
                    assertTrue(response.shortId.length == 8)
                }
        }

        @Test
        fun `should handle URL with maximum length`() {
            // Test with a 2048 character URL (your max limit)
            val longUrl = "https://example.com/" + "a".repeat(2028)
            val request = CreateUrlRequest(longUrl)

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody<UrlResponse>()
                .value { response ->
                    assertEquals(longUrl, response.originalUrl)
                    assertTrue(response.shortId.length == 8)
                }
        }

        @ParameterizedTest(name = "should reject invalid URL: {0}")
        @ValueSource(
            strings = [
                "not-a-url",
                "ftp://invalid-scheme.com",
                "http://",
                "https://",
                "http://.com",
                "http://example.",
                "http://ex ample.com",
                "http://example.com/path with spaces",
                "http://example.com\\invalid-backslash",
                "http://example.com:invalid-port",
                "http://[invalid-ipv6].com",
            ],
        )
        fun `should reject invalid URLs`(invalidUrl: String) {
            val request = CreateUrlRequest(invalidUrl)

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertEquals("Validation Error", error.error)
                    assertTrue(error.message.contains("Invalid URL format"))
                }
        }

        @Test
        fun `should reject URL exceeding maximum length`() {
            val longUrl = "https://example.com/" + "a".repeat(2049 - "https://example.com/".length)
            val request = CreateUrlRequest(longUrl)

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertEquals("Validation Error", error.error)
                    assertTrue(error.message.contains("URL is too long"))
                }
        }

        @Test
        fun `should reject empty URL`() {
            val request = CreateUrlRequest("")

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertEquals("Validation Error", error.error)
                    assertTrue(error.message.contains("URL cannot be empty"))
                }
        }

        @Test
        fun `should reject blank URL`() {
            val request = CreateUrlRequest("   ")

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertEquals("Validation Error", error.error)
                    assertTrue(error.message.contains("URL cannot be empty"))
                }
        }

        @Test
        fun `should reject URL with too long domain label`() {
            val longLabel = "a".repeat(64)
            val request = CreateUrlRequest("https://$longLabel.example.com")

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertTrue(error.message.contains("Invalid URL format"))
                }
        }

        @Test
        fun `should handle URL with maximum allowed domain label length`() {
            val maxLabel = "a".repeat(63)
            val request = CreateUrlRequest("https://$maxLabel.example.com")

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody<UrlResponse>()
                .value { response ->
                    assertTrue(response.shortId.length == 8)
                }
        }

        @Test
        fun `should handle URL with long query parameters`() {
            val longQueryParam = "param=" + "x".repeat(1000)
            val request = CreateUrlRequest("https://example.com/path?$longQueryParam")

            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody<UrlResponse>()
                .value { response ->
                    assertTrue(response.shortId.length == 8)
                }
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `should handle bad request for malformed JSON`() {
            // Arrange
            val malformedJson = """{"originalUrl": }""" // Invalid JSON

            // Act & Assert
            webTestClient.post()
                .uri("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(malformedJson)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(400, error.status)
                    assertTrue(error.message.contains("Invalid short ID format: must be exactly"))
                }
        }

        @Test
        fun `should return 404 for non-existent shortId`() {
            val validNonExistingId = "VIuYpnMm"
            webTestClient.get()
                .uri("/api/v1/urls/$validNonExistingId")
                .exchange()
                .expectStatus().isNotFound
                .expectBody<ErrorResponse>()
                .value { error ->
                    assertEquals(404, error.status)
                    assertTrue(error.message.contains(validNonExistingId))
                }
        }
    }

    @Nested
    inner class Consistency {
        @Test
        fun `should maintain data consistency after multiple operations`() {
            // Arrange
            val baseUrl = "https://example.com/page"
            val numberOfOperations = 5

            // Act - Create multiple URLs
            val createdUrls =
                (1..numberOfOperations).map { i ->
                    val url = "$baseUrl$i"
                    val request = CreateUrlRequest(url)

                    webTestClient.post()
                        .uri("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<UrlResponse>()
                        .returnResult()
                        .responseBody!!
                }

            // Verify each URL can be resolved
            createdUrls.forEach { created ->
                webTestClient.get()
                    .uri("/api/v1/urls/${created.shortId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<UrlResponse>()
                    .value { response ->
                        assertEquals(created.originalUrl, response.originalUrl)
                        assertEquals(created.shortId, response.shortId)
                    }
            }

            // Verify database state
            StepVerifier.create(repository.count())
                .expectNext(numberOfOperations.toLong())
                .verifyComplete()

            // Verify all entries are retrievable and correct
            StepVerifier.create(repository.findAll())
                .expectNextCount(numberOfOperations.toLong())
                .verifyComplete()
        }
    }

    @Nested
    inner class Concurrency {
        @Test
        fun `should handle concurrent requests for different URLs`() {
            // Arrange
            val numberOfRequests = 10
            val baseUrl = "https://example.com/page"
            val requests =
                (1..numberOfRequests).map { i ->
                    CreateUrlRequest("$baseUrl$i")
                }

            // Act - Send truly concurrent requests
            val responses =
                runBlocking {
                    requests.map { request ->
                        async {
                            webTestClient.post()
                                .uri("/api/v1/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isOk
                                .expectBody<UrlResponse>()
                                .returnResult()
                                .responseBody!!
                        }
                    }.awaitAll()
                }

            // Assert - All responses should have different shortIds
            val shortIds = responses.map { it.shortId }.toSet()
            assertEquals(numberOfRequests, shortIds.size, "Each URL should have a unique shortId")

            // Verify URLs match their requests
            responses.forEachIndexed { index, response ->
                assertEquals("$baseUrl${index + 1}", response.originalUrl)
            }

            // Verify database has correct number of entries
            StepVerifier.create(repository.count())
                .expectNext(numberOfRequests.toLong())
                .verifyComplete()

            // Verify each URL can be resolved
            responses.forEach { created ->
                webTestClient.get()
                    .uri("/api/v1/urls/${created.shortId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<UrlResponse>()
                    .value { response ->
                        assertEquals(created.originalUrl, response.originalUrl)
                        assertEquals(created.shortId, response.shortId)
                    }
            }
        }

        @Test
        fun `should handle concurrent requests for same URL`() {
            // Arrange
            val url = "https://example.com"
            val numberOfRequests = 10
            val request = CreateUrlRequest(url)

            // Act - Send truly concurrent requests
            val responses =
                runBlocking {
                    (1..numberOfRequests)
                        .map {
                            async {
                                webTestClient.post()
                                    .uri("/api/v1/urls")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(request)
                                    .exchange()
                                    .expectStatus().isOk
                                    .expectBody<UrlResponse>()
                                    .returnResult()
                                    .responseBody!!
                            }
                        }
                        .awaitAll()
                }

            // Assert - All responses should have the same shortId
            val firstShortId = responses.first().shortId
            responses.forEach { response ->
                assertEquals(url, response.originalUrl)
                assertEquals(firstShortId, response.shortId)
            }

            // Verify database has only one entry
            StepVerifier.create(repository.count())
                .expectNext(1)
                .verifyComplete()
        }
    }
}
