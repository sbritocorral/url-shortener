package com.codefactory.urlshortener.api.response

data class UrlResponse(
    val shortId: String,
    val originalUrl: String,
)
