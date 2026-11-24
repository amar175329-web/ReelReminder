package com.reelreminder.app.util

import java.net.URI

/**
 * Instagram and most other share sheets send a blob of text such as
 * "Check out this reel: https://www.instagram.com/reel/ABC/?igsh=...".
 * We only want valid Instagram reel, post (p), or tv links over HTTPS.
 */
object UrlExtractor {
    private val urlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    private const val trailingPunctuation = ".,)]};:!?\"'…"

    private val instagramHostRegex = Regex("""^(?:www\.)?instagram\.com$""", RegexOption.IGNORE_CASE)
    private val instagramPathRegex = Regex("""^/(?:reel|reels|p|tv)/[\w\-]+.*""", RegexOption.IGNORE_CASE)

    fun extractFirstUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val match = urlRegex.find(text)?.value ?: return null
        val cleaned = match.trimEnd { it in trailingPunctuation }
        return if (isValidInstagramUrl(cleaned)) cleaned else null
    }

    /**
     * Validates that the link is strictly an Instagram HTTPS link to a reel, post, or tv.
     * Brief M06 49.2 rule 5: accept only https host instagram.com / www.instagram.com (reel, p, tv paths).
     */
    fun isValidInstagramUrl(value: String): Boolean {
        val candidate = value.trim()
        if (candidate.isEmpty() || candidate.any { it.isWhitespace() }) return false
        return runCatching {
            val uri = URI(candidate)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()
            val path = uri.rawPath ?: uri.path ?: ""
            scheme == "https" &&
                host != null &&
                instagramHostRegex.matches(host) &&
                instagramPathRegex.matches(path)
        }.getOrDefault(false)
    }

    fun isValidHttpUrl(value: String): Boolean = isValidInstagramUrl(value)

    /** Returns the trimmed URL when it is a valid Instagram link, otherwise null. */
    fun validHttpUrlOrNull(value: String): String? {
        val candidate = value.trim()
        return if (isValidInstagramUrl(candidate)) candidate else null
    }
}
