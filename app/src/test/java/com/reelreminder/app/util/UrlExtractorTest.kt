package com.reelreminder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlExtractorTest {

    @Test
    fun extractsFirstInstagramUrlFromShareText() {
        val text = "Check out this reel: https://www.instagram.com/reel/ABC123/?igsh=xyz — so good!"
        assertEquals(
            "https://www.instagram.com/reel/ABC123/?igsh=xyz",
            UrlExtractor.extractFirstUrl(text)
        )
    }

    @Test
    fun trimsTrailingPunctuation() {
        assertEquals(
            "https://www.instagram.com/reel/ABC123",
            UrlExtractor.extractFirstUrl("(https://www.instagram.com/reel/ABC123).")
        )
    }

    @Test
    fun acceptsReelPostAndTvPaths() {
        assertTrue(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/reel/ABC123"))
        assertTrue(UrlExtractor.isValidInstagramUrl("https://instagram.com/reel/ABC123/"))
        assertTrue(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/p/XYZ789/"))
        assertTrue(UrlExtractor.isValidInstagramUrl("https://instagram.com/tv/DEF456/"))
        assertTrue(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/reels/GHI012/"))
    }

    @Test
    fun preservesQueryParameters() {
        val url = "https://www.instagram.com/reel/ABC123/?igsh=MzRlODBiNWFlZA=="
        assertEquals(url, UrlExtractor.extractFirstUrl("Look: $url"))
    }

    @Test
    fun rejectsNonHttps() {
        assertFalse(UrlExtractor.isValidInstagramUrl("http://www.instagram.com/reel/ABC123"))
    }

    @Test
    fun rejectsNonInstagramHosts() {
        assertFalse(UrlExtractor.isValidInstagramUrl("https://example.com/reel/ABC123"))
        assertFalse(UrlExtractor.isValidInstagramUrl("https://malicious-instagram.com/reel/ABC123"))
        assertFalse(UrlExtractor.isValidInstagramUrl("https://youtube.com/shorts/12345"))
    }

    @Test
    fun rejectsNonReelPaths() {
        assertFalse(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/stories/username/"))
        assertFalse(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/explore/"))
        assertFalse(UrlExtractor.isValidInstagramUrl("https://www.instagram.com/username/"))
    }

    @Test
    fun returnsNullWhenThereIsNoLink() {
        assertNull(UrlExtractor.extractFirstUrl("just some text"))
        assertNull(UrlExtractor.extractFirstUrl(null))
        assertNull(UrlExtractor.extractFirstUrl(""))
    }
}
