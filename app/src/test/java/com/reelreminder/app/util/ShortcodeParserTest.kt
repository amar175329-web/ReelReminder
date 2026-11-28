package com.reelreminder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcodeParserTest {

    @Test
    fun parsesStandardReelUrlAndDropsTrackingParams() {
        val parsed = ShortcodeParser.parse(
            "https://www.instagram.com/reel/Di4nRt6AbCd/?igsh=MWx2YzM0&utm_source=ig_web_copy_link"
        )
        assertEquals("Di4nRt6AbCd", parsed?.shortcode)
        assertEquals("https://www.instagram.com/reel/Di4nRt6AbCd/", parsed?.canonicalUrl)
    }

    @Test
    fun parsesReelsPostAndTvPaths() {
        val reels = ShortcodeParser.parse("https://instagram.com/reels/AbC_123-xyz/")
        assertEquals("AbC_123-xyz", reels?.shortcode)
        assertEquals("https://www.instagram.com/reel/AbC_123-xyz/", reels?.canonicalUrl)

        val post = ShortcodeParser.parse("https://www.instagram.com/p/CxYz987654/")
        assertEquals("CxYz987654", post?.shortcode)
        assertEquals("https://www.instagram.com/reel/CxYz987654/", post?.canonicalUrl)

        val tv = ShortcodeParser.parse("https://instagram.com/tv/TvCode12345/?foo=bar")
        assertEquals("TvCode12345", tv?.shortcode)
        assertEquals("https://www.instagram.com/reel/TvCode12345/", tv?.canonicalUrl)
    }

    @Test
    fun parsesSharedTextContainingInstagramLink() {
        val parsed = ShortcodeParser.extractFromText(
            "Check out this reel! https://www.instagram.com/reel/C8xYz_12-ab/?igsh=12345..."
        )
        assertEquals("C8xYz_12-ab", parsed?.shortcode)
        assertEquals("https://www.instagram.com/reel/C8xYz_12-ab/", parsed?.canonicalUrl)
    }

    @Test
    fun rejectsNonInstagramDomainsAndInvalidPaths() {
        assertNull(ShortcodeParser.parse("https://youtube.com/reel/Di4nRt6AbCd/"))
        assertNull(ShortcodeParser.parse("https://notinstagram.com/reel/Di4nRt6AbCd/"))
        assertNull(ShortcodeParser.parse("https://instagram.com.evil.com/reel/Di4nRt6AbCd/"))
        assertNull(ShortcodeParser.parse("http://www.instagram.com/reel/Di4nRt6AbCd/"))
        assertNull(ShortcodeParser.parse("https://www.instagram.com/explore/"))
        assertNull(ShortcodeParser.parse("https://www.instagram.com/reel/abc/")) // < 5 chars
        assertNull(ShortcodeParser.parse(""))
        assertNull(ShortcodeParser.parse("   "))
    }
}
