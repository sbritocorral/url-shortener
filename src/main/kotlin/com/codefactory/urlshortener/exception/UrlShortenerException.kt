package com.codefactory.urlshortener.exception

sealed class UrlShortenerException(message: String) : RuntimeException(message)

class UrlNotFoundException(shortId: String) :
    UrlShortenerException("URL not found for short ID: $shortId")
