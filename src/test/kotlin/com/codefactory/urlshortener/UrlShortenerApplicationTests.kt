package com.codefactory.urlshortener

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

/**
 * Application startup test.
 * Verifies the Spring context loads successfully.
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class UrlShortenerApplicationTests {
    @Test
    fun `application main should start without throwing exceptions`() {
        main(arrayOf())
    }
}
