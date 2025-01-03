package com.codefactory.urlshortener.util

import com.codefactory.urlshortener.constants.UrlConstants.BASE62_ALPHABET
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.collections.forEach
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.text.repeat
import kotlin.text.takeLast

class ShortIdGeneratorTest {
    @Test
    fun `generateShortId should create consistent output for same input`() {
        // Arrange
        val input = "https://example.com"

        // Act
        val result1 = ShortIdGenerator.generateShortId(input)
        val result2 = ShortIdGenerator.generateShortId(input)

        // Assert
        assertEquals(result1, result2)
        assertEquals(8, result1.length)
    }

    @Test
    fun `generateShortId should create consistent length output for all inputs`() {
        // Arrange
        val testUrls =
            listOf(
                "https://example.com",
                "https://example.com/",
                "https://example.com/path",
                "https://example.com?param=value",
                "https://example.com#fragment",
            )

        // Act & Assert
        testUrls.forEach { url ->
            val result = ShortIdGenerator.generateShortId(url)
            assertEquals(
                8,
                result.length,
                "Generated shortId for '$url' has incorrect length: $result (length: ${result.length})",
            )
        }
    }

    @Test
    fun `generateShortId should maintain consistent length with special characters`() {
        // Arrange
        val testUrls =
            listOf(
                "https://example.com/!@#$%^&*()",
                "https://example.com#fragment1234567890",
                "https://example.com/very/long/path/with#fragment?and=param",
                "https://example.com/" + "a".repeat(100),
            )

        // Act & Assert
        testUrls.forEach { url ->
            val result = ShortIdGenerator.generateShortId(url)
            assertEquals(
                8,
                result.length,
                "Generated shortId for URL ending with '${url.takeLast(20)}...' has incorrect length: $result (length: ${result.length})",
            )
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [8, 10, 16, 32])
    fun `generateShortId should respect different custom lengths`(length: Int) {
        // Arrange
        val input = "https://example.com"

        // Act
        val result = ShortIdGenerator.generateShortId(input, length)

        // Assert
        assertEquals(length, result.length)
    }

    @Test
    fun `generateShortId should create different outputs for slightly different URLs`() {
        // Arrange
        val url1 = "https://example.com#fragment1"
        val url2 = "https://example.com#fragment2"

        // Act
        val result1 = ShortIdGenerator.generateShortId(url1)
        val result2 = ShortIdGenerator.generateShortId(url2)

        // Assert
        assertNotEquals(result1, result2)
        assertEquals(8, result1.length)
        assertEquals(8, result2.length)
    }

    @Test
    fun `generateShortId should only use valid base62 characters`() {
        // Arrange
        val input = "https://example.com"
        val customLength = 1000

        // Act
        val result = ShortIdGenerator.generateShortId(input, customLength)

        // Assert
        assertTrue(
            result.all { it in BASE62_ALPHABET },
            "Generated ID contains invalid characters: ${result.filter { it !in BASE62_ALPHABET }}",
        )
        assertEquals(customLength, result.length)
    }
}
