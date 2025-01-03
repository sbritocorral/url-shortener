package com.codefactory.urlshortener.util

import com.codefactory.urlshortener.constants.UrlConstants
import java.security.MessageDigest
import kotlin.collections.fold
import kotlin.collections.take
import kotlin.collections.toByteArray
import kotlin.text.padStart
import kotlin.text.substring
import kotlin.text.toByteArray

object ShortIdGenerator {
    fun generateShortId(
        input: String,
        length: Int = UrlConstants.SHORT_ID_LENGTH,
    ): String {
        val sha256 =
            MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray())

        // Take first 6 bytes (gives us ~281 trillion combinations with base62)
        val truncatedBytes = sha256.take(6).toByteArray()
        val base62 = base62Encode(truncatedBytes)

        // Ensure consistent length
        return when {
            base62.length > length -> base62.substring(0, length)
            base62.length < length -> base62.padStart(length, UrlConstants.BASE62_ALPHABET[0])
            else -> base62
        }
    }

    private fun base62Encode(bytes: ByteArray): String {
        var number = bytes.fold(0L) { acc, b -> (acc shl 8) or (b.toLong() and 0xFF) }
        val sb = StringBuilder()

        // Handle the case when number is 0
        if (number == 0L) {
            return UrlConstants.BASE62_ALPHABET[0].toString()
        }

        while (number > 0) {
            val remainder = (number % 62).toInt()
            sb.append(UrlConstants.BASE62_ALPHABET[remainder])
            number /= 62
        }

        return sb.reverse().toString()
    }
}
