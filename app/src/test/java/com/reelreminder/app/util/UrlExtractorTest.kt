package com.reelreminder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlExtractorTest {

    @Test
    fun extractsFirstUrlFromShareText() {
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
    fun returnsNullWhenThereIsNoLink() {
        assertNull(UrlExtractor.extractFirstUrl("just some text"))
        assertNull(UrlExtractor.extractFirstUrl(null))
        assertNull(UrlExtractor.extractFirstUrl(""))
    }

    @Test
    fun acceptsHttpAndHttpsLinks() {
        assertTrue(UrlExtractor.isValidHttpUrl("https://www.instagram.com/reel/ABC123"))
        assertTrue(UrlExtractor.isValidHttpUrl("http://example.com/video?id=1"))
    }

    @Test
    fun rejectsLinksWithoutAWebSchemeOrHost() {
        assertFalse(UrlExtractor.isValidHttpUrl("instagram.com/reel/ABC123"))
        assertFalse(UrlExtractor.isValidHttpUrl("ftp://example.com/file"))
        assertFalse(UrlExtractor.isValidHttpUrl("https://"))
        assertFalse(UrlExtractor.isValidHttpUrl(""))
        assertFalse(UrlExtractor.isValidHttpUrl("https://example.com/has space"))
    }

    @Test
    fun returnsOnlyValidTrimmedUrl() {
        assertEquals(
            "https://example.com/a",
            UrlExtractor.validHttpUrlOrNull("  https://example.com/a  ")
        )
        assertNull(UrlExtractor.validHttpUrlOrNull("not a url"))
    }
}
