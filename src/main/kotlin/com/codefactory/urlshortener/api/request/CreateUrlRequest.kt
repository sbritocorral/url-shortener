package com.codefactory.urlshortener.api.request

import com.codefactory.urlshortener.validation.ValidUrl
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUrlRequest(
    @field:NotBlank(message = "URL cannot be empty")
    @field:ValidUrl
    @field:Size(max = 2048, message = "URL is too long")
    val originalUrl: String,
)
