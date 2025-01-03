package com.codefactory.urlshortener.controller

import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.api.response.ErrorResponse
import com.codefactory.urlshortener.api.response.UrlResponse
import com.codefactory.urlshortener.domain.toResponse
import com.codefactory.urlshortener.service.UrlShortenerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Shortener", description = "API for shortening and resolving URLs")
class UrlShortenerController(private val service: UrlShortenerService) {
    @Operation(
        summary = "Create short URL",
        description = "Creates a shortened URL from a long URL. Returns existing mapping if URL was already shortened.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "URL successfully shortened",
                content = [Content(schema = Schema(implementation = UrlResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid URL format",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createUrl(
        @Valid @RequestBody request: CreateUrlRequest,
    ): Mono<UrlResponse> {
        return service.createShortUrl(request)
            .map { it.toResponse() }
    }

    @Operation(
        summary = "Resolve short URL",
        description = "Retrieves the original URL for a given short ID",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Original URL found",
                content = [Content(schema = Schema(implementation = UrlResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid short ID format - must be 8 characters of [A-Za-z0-9]",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Short URL not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{shortId}")
    fun getUrl(
        @Parameter(
            description = "Short ID to resolve",
            example = "Ab12Cd3E",
            required = true,
        ) @PathVariable shortId: String,
    ): Mono<UrlResponse> {
        return service.resolveShortUrl(shortId)
            .map { it.toResponse() }
    }
}
