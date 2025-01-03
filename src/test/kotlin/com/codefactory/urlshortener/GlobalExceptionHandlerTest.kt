package com.codefactory.urlshortener

import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.api.response.ErrorResponse
import com.codefactory.urlshortener.controller.UrlShortenerController
import com.codefactory.urlshortener.service.UrlShortenerService
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@WebFluxTest(controllers = [UrlShortenerController::class])
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @TestConfiguration
    class TestConfig {
        @Bean
        fun urlShortenerService() = mockk<UrlShortenerService>()
    }

    @Autowired
    private lateinit var urlShortenerService: UrlShortenerService

    @Test
    fun `should handle unexpected exceptions`() {
        // Arrange
        val request = CreateUrlRequest("https://example.com")
        coEvery { urlShortenerService.createShortUrl(any()) } returns Mono.error(
            RuntimeException("Unexpected database error")
        )

        // Act & Assert
        webTestClient.post()
            .uri("/api/v1/urls")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectBody<ErrorResponse>()
            .value { error ->
                assertEquals(500, error.status)
                assertEquals("Internal Server Error", error.error)
                assertEquals("An unexpected error occurred", error.message)
                assertTrue(error.path.contains("/api/v1/urls"))
            }
    }

    @Test
    fun `should handle unexpected exceptions in URL resolution`() {
        // Arrange
        val shortId = "abcd1234"
        coEvery { urlShortenerService.resolveShortUrl(any()) } returns Mono.error(
            RuntimeException("Unexpected database error")
        )

        // Act & Assert
        webTestClient.get()
            .uri("/api/v1/urls/$shortId")
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectBody<ErrorResponse>()
            .value { error ->
                assertEquals(500, error.status)
                assertEquals("Internal Server Error", error.error)
                assertEquals("An unexpected error occurred", error.message)
                assertTrue(error.path.contains("/api/v1/urls/$shortId"))
            }
    }
}
