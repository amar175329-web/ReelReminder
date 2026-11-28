package com.reelreminder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareCaptureParserTest {

    @Test
    fun extractsCanonicalLinkFromRealWorldInstagramShareText() {
        val raw = "See this Instagram reel by @creator https://www.instagram.com/reel/Di4nRt6AbCd/?igsh=MWx2YzM0\nSent from Instagram"
        val snap = ShareCaptureParser.parse(
            rawText = raw,
            mimeType = "text/plain",
            extraKeys = listOf("android.intent.extra.TEXT", "android.intent.extra.SUBJECT")
        )
        assertEquals("text/plain", snap.mimeType)
        assertEquals("Di4nRt6AbCd", snap.shortcode)
        assertEquals("https://www.instagram.com/reel/Di4nRt6AbCd/", snap.extractedUrl)
        assertEquals(listOf("android.intent.extra.SUBJECT", "android.intent.extra.TEXT"), snap.extraKeys)
        assertFalse(snap.hasStreamExtra)
    }

    @Test
    fun detectsStreamExtraKeyWithoutStoringRawStreamContent() {
        val snap = ShareCaptureParser.parse(
            rawText = "https://instagram.com/reels/AbCdE12345/",
            mimeType = "text/plain",
            extraKeys = listOf("android.intent.extra.TEXT", "android.intent.extra.STREAM")
        )
        assertEquals("AbCdE12345", snap.shortcode)
        assertTrue(snap.hasStreamExtra)
    }

    @Test
    fun rejectsNonInstagramLinks() {
        val snap = ShareCaptureParser.parse(
            rawText = "Watch https://youtu.be/dQw4w9WgXcQ",
            mimeType = "text/plain",
            extraKeys = listOf("android.intent.extra.TEXT")
        )
        assertNull(snap.extractedUrl)
        assertNull(snap.shortcode)
    }
}
