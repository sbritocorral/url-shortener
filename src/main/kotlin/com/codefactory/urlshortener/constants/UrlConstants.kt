package com.codefactory.urlshortener.constants

object UrlConstants {
    const val SHORT_ID_LENGTH = 8
    const val BASE62_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    const val SHORT_ID_PATTERN = "^[$BASE62_ALPHABET]{$SHORT_ID_LENGTH}$"
    const val SHORT_ID_MESSAGE = "Short ID must be exactly $SHORT_ID_LENGTH characters long and contain only letters and numbers"
}
