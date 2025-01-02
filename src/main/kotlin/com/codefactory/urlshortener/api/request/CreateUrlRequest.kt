package com.codefactory.urlshortener.api.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class CreateUrlRequest(
    @field:NotBlank(message = "URL cannot be empty")
    @field:URL(message = "Invalid URL format")
    @field:Size(max = 2048, message = "URL is too long")
    val originalUrl: String,
)
