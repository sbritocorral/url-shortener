package com.codefactory.urlshortener.exception

/**
 * Base class for all URL shortener specific exceptions.
 * Provides common structure for service-specific errors.
 */
sealed class UrlShortenerException(message: String) : RuntimeException(message)

/**
 * Thrown when attempting to resolve a non-existent short URL.
 */
class UrlNotFoundException(shortId: String) :
    UrlShortenerException("URL not found for short ID: $shortId")
