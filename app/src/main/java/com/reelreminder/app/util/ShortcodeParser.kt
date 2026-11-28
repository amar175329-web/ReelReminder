package com.reelreminder.app.util

import java.net.URI

data class ParsedInstagramReel(
    val shortcode: String,
    val canonicalUrl: String
)

/**
 * Extracts and validates Instagram reel/post/tv shortcodes and builds canonical URLs
 * with tracking query parameters stripped.
 */
object ShortcodeParser {
    private val urlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    private const val trailingPunctuation = ".,)]};:!?\"'…"
    private val hostRegex = Regex("""^(?:www\.)?instagram\.com$""", RegexOption.IGNORE_CASE)
    private val shortcodeRegex = Regex(
        """instagram\.com/(?:reel|reels|p|tv)/([A-Za-z0-9_-]{5,})""",
        RegexOption.IGNORE_CASE
    )

    fun parse(rawUrl: String?): ParsedInstagramReel? {
        val candidate = rawUrl?.trim().orEmpty()
        if (candidate.isEmpty() || candidate.any { it.isWhitespace() }) return null
        return runCatching {
            val uri = URI(candidate)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()
            if (scheme != "https" || host == null || !hostRegex.matches(host)) {
                return null
            }
            val match = shortcodeRegex.find(candidate) ?: return null
            val shortcode = match.groupValues[1]
            ParsedInstagramReel(
                shortcode = shortcode,
                canonicalUrl = "https://www.instagram.com/reel/$shortcode/"
            )
        }.getOrNull()
    }

    fun extractFromText(text: String?): ParsedInstagramReel? {
        if (text.isNullOrBlank()) return null
        val match = urlRegex.find(text)?.value ?: return null
        val cleaned = match.trimEnd { it in trailingPunctuation }
        return parse(cleaned)
    }
}
