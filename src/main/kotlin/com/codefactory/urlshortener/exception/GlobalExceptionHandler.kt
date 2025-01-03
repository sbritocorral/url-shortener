package com.codefactory.urlshortener.exception

import com.codefactory.urlshortener.api.response.ErrorResponse
import com.codefactory.urlshortener.constants.UrlConstants
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(UrlShortenerException::class)
    fun handleUrlShortenerException(
        ex: UrlShortenerException,
        request: ServerHttpRequest,
    ): ResponseEntity<ErrorResponse> {
        val status =
            when (ex) {
                is UrlNotFoundException -> HttpStatus.NOT_FOUND
            }

        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = ex.message ?: "An error occurred",
                    path = request.path.pathWithinApplication().value(),
                ),
            )
    }

    @ExceptionHandler(ServerWebInputException::class, ResponseStatusException::class, ConstraintViolationException::class)
    fun handlePathVariableException(
        ex: Exception,
        request: ServerHttpRequest,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Invalid Request",
                    message =
                        "Invalid short ID format: must be exactly ${UrlConstants.SHORT_ID_LENGTH} characters long " +
                            "and contain only letters and numbers",
                    path = request.path.pathWithinApplication().value(),
                ),
            )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationErrors(
        ex: WebExchangeBindException,
        request: ServerHttpRequest,
    ): ResponseEntity<ErrorResponse> {
        val errors =
            ex.bindingResult.fieldErrors
                .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Error",
                    message = errors,
                    path = request.path.pathWithinApplication().value(),
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericError(
        ex: Exception,
        request: ServerHttpRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                    path = request.path.pathWithinApplication().value(),
                ),
            )
    }
}
