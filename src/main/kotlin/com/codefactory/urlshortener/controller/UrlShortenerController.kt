package com.codefactory.urlshortener.controller


import com.codefactory.urlshortener.api.request.CreateUrlRequest
import com.codefactory.urlshortener.api.response.UrlResponse
import com.codefactory.urlshortener.constants.UrlConstants
import com.codefactory.urlshortener.domain.toResponse
import com.codefactory.urlshortener.service.UrlShortenerService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
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
class UrlShortenerController(
    private val service: UrlShortenerService,
) {
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createUrl(
        @Valid @RequestBody request: CreateUrlRequest,
    ): Mono<UrlResponse> {
        return service.createShortUrl(request)
            .map { it.toResponse() }
    }

    @GetMapping("/{shortId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUrl(
        @PathVariable
        @Pattern(
            regexp = UrlConstants.SHORT_ID_PATTERN,
            message = UrlConstants.SHORT_ID_MESSAGE
        )
        shortId: String,
    ): Mono<UrlResponse> {
        return service.resolveShortUrl(shortId)
            .map { it.toResponse() }
    }
}
