package com.codefactory.urlshortener

import org.springframework.boot.fromApplication
import org.springframework.boot.with

/**
 * Test application entry point.
 * Configures the application with test-specific components.
 */
fun main(args: Array<String>) {
    fromApplication<UrlShortenerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
