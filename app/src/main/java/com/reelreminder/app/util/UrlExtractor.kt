package com.reelreminder.app.util

import java.net.URI

/**
 * Instagram and most other share sheets send a blob of text such as
 * "Check out this reel: https://www.instagram.com/reel/ABC/?igsh=...".
 * We only want the first web link.
 */
object UrlExtractor {
    private val urlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    private const val trailingPunctuation = ".,)]};:!?\"'…"

    fun extractFirstUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val match = urlRegex.find(text)?.value ?: return null
        return match.trimEnd { it in trailingPunctuation }
    }

    fun isValidHttpUrl(value: String): Boolean {
        val candidate = value.trim()
        if (candidate.isEmpty() || candidate.any { it.isWhitespace() }) return false
        return runCatching {
            val uri = URI(candidate)
            val scheme = uri.scheme?.lowercase()
            (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    /** Returns the trimmed URL when it is a valid web link, otherwise null. */
    fun validHttpUrlOrNull(value: String): String? {
        val candidate = value.trim()
        return if (isValidHttpUrl(candidate)) candidate else null
    }
}
