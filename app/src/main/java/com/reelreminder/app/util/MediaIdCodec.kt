package com.reelreminder.app.util

import java.math.BigInteger

/**
 * Converts between Instagram base64url shortcodes and numeric media IDs.
 * Note: instagram://media?id=<mediaId> is undocumented (UNKNOWN until device-tested, ADR-3).
 */
object MediaIdCodec {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    private val BASE = BigInteger.valueOf(64L)

    fun shortcodeToMediaId(shortcode: String): String? {
        if (shortcode.isEmpty()) return null
        var value = BigInteger.ZERO
        for (ch in shortcode) {
            val index = ALPHABET.indexOf(ch)
            if (index < 0) return null
            value = value.multiply(BASE).add(BigInteger.valueOf(index.toLong()))
        }
        return value.toString()
    }

    fun mediaIdToShortcode(mediaId: String): String? {
        if (mediaId.isEmpty() || !mediaId.all { it.isDigit() }) return null
        var value = runCatching { BigInteger(mediaId) }.getOrNull() ?: return null
        if (value < BigInteger.ZERO) return null
        if (value == BigInteger.ZERO) return ALPHABET[0].toString()

        val sb = StringBuilder()
        while (value > BigInteger.ZERO) {
            val (quotient, remainder) = value.divideAndRemainder(BASE)
            sb.append(ALPHABET[remainder.toInt()])
            value = quotient
        }
        return sb.reverse().toString()
    }
}
