package com.codefactory.urlshortener.constants

/**
 * Constants used throughout the URL shortening service.
 * Defines key configuration values and validation patterns.
 */
object UrlConstants {
    /**
     * Length of generated short IDs
     */
    const val SHORT_ID_LENGTH = 8

    /**
     * Characters used for Base62 encoding
     */
    const val BASE62_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    /**
     * Regex pattern for validating short IDs
     */
    const val SHORT_ID_PATTERN = "^[$BASE62_ALPHABET]{$SHORT_ID_LENGTH}$"

    /**
     * Validation error message for invalid short IDs
     */
    const val SHORT_ID_MESSAGE = "Short ID must be exactly $SHORT_ID_LENGTH characters long and contain only letters and numbers"
}
