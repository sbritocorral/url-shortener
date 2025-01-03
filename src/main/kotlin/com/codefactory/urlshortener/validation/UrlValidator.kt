package com.codefactory.urlshortener.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.net.URI
import java.net.URISyntaxException

class UrlValidator : ConstraintValidator<ValidUrl, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null || value.isBlank()) return true // Let @NotBlank handle this

        return try {
            val uri = URI(value)

            // Check scheme
            if (uri.scheme !in listOf("http", "https")) return false

            // Must have host
            val host = uri.host ?: return false

            // Check raw authority (host:port) for invalid characters
            val authority = uri.authority ?: return false
            if (authority.contains('#')) return false

            // Check host length
            if (host.length > 253) return false

            // Check domain labels
            host.split('.').forEach { label ->
                if (label.length > 63 || label.isEmpty()) return false
            }

            // No double dots
            if (host.contains("..")) return false

            // Valid port
            if (uri.port != -1 && (uri.port !in 1..65535)) return false

            true
        } catch (e: URISyntaxException) {
            false
        }
    }
}
