package com.reelreminder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaIdCodecTest {

    @Test
    fun decodeAndEncodeRoundTrip() {
        val samples = listOf(
            "Di4nRt6AbCd",
            "CxYz987654",
            "B-12345_aZ",
            "C8xYz_12-ab"
        )
        samples.forEach { code ->
            val mediaId = MediaIdCodec.shortcodeToMediaId(code)
            assertEquals(code, MediaIdCodec.mediaIdToShortcode(mediaId!!))
        }
    }

    @Test
    fun knownInstagramShortcodeDecodesCorrectly() {
        // B = 1, A = 0 -> 1 * 64 + 0 = 64
        assertEquals("64", MediaIdCodec.shortcodeToMediaId("BA"))
        assertEquals("BA", MediaIdCodec.mediaIdToShortcode("64"))
    }

    @Test
    fun rejectsInvalidShortcodeOrMediaId() {
        assertNull(MediaIdCodec.shortcodeToMediaId(""))
        assertNull(MediaIdCodec.shortcodeToMediaId("invalid!@#"))
        assertNull(MediaIdCodec.mediaIdToShortcode(""))
        assertNull(MediaIdCodec.mediaIdToShortcode("-5"))
        assertNull(MediaIdCodec.mediaIdToShortcode("abc"))
    }
}
