package com.reelreminder.app.util

import com.reelreminder.app.data.Reel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReelOpenerTest {

    private data class LaunchCall(
        val method: OpenMethod,
        val reelId: Long,
        val shortcode: String,
        val uri: String
    )

    @Test
    fun openLoadsReelByIdAndPassesStoredShortcodeAndCanonicalUri() = runBlocking {
        val reels = mapOf(
            1L to Reel(id = 1L, url = "https://www.instagram.com/reel/FirstCode1/?igsh=aaa", note = "Note A"),
            2L to Reel(id = 2L, url = "https://www.instagram.com/reel/SecondCode2/?igsh=bbb", note = "Note B"),
            3L to Reel(id = 3L, url = "https://www.instagram.com/p/ThirdCode3/?igsh=ccc", note = "Note C")
        )
        val calls = mutableListOf<LaunchCall>()
        val logs = mutableListOf<String>()

        val opener = ReelOpener(
            loadReelById = { id -> reels[id] },
            getOpenMethod = { OpenMethod.M2_INSTAGRAM_HTTPS },
            launcher = object : ReelLaunchDelegate {
                override fun launch(method: OpenMethod, reelId: Long, shortcode: String, uri: String): Boolean {
                    calls.add(LaunchCall(method, reelId, shortcode, uri))
                    return true
                }
            },
            logger = { msg -> logs.add(msg) }
        )

        val result2 = opener.open(2L)
        assertTrue(result2 is OpenResult.Opened)
        assertEquals(1, calls.size)
        assertEquals(2L, calls[0].reelId)
        assertEquals("SecondCode2", calls[0].shortcode)
        assertEquals("https://www.instagram.com/reel/SecondCode2/", calls[0].uri)
        assertEquals(OpenMethod.M2_INSTAGRAM_HTTPS, calls[0].method)

        // Ensure log contains id, shortcode, uri and NEVER personal note text
        assertTrue(logs.any { it.contains("id=2") && it.contains("shortcode=SecondCode2") })
        assertTrue(logs.none { it.contains("Note B") })
    }

    @Test
    fun openWithM1UsesDecodedMediaIdUri() = runBlocking {
        val reel = Reel(id = 7L, url = "https://www.instagram.com/reel/BA12345/", note = "Private")
        val calls = mutableListOf<LaunchCall>()
        val expectedMediaId = MediaIdCodec.shortcodeToMediaId("BA12345")!!

        val opener = ReelOpener(
            loadReelById = { reel },
            getOpenMethod = { OpenMethod.M1_DEEP_LINK },
            launcher = object : ReelLaunchDelegate {
                override fun launch(method: OpenMethod, reelId: Long, shortcode: String, uri: String): Boolean {
                    calls.add(LaunchCall(method, reelId, shortcode, uri))
                    return true
                }
            },
            logger = {}
        )

        val result = opener.open(7L)
        assertTrue(result is OpenResult.Opened)
        assertEquals("instagram://media?id=$expectedMediaId", calls[0].uri)
    }

    @Test
    fun openWithInAppPlayerUsesEmbedUri() = runBlocking {
        val reel = Reel(id = 9L, url = "https://www.instagram.com/reel/EmbedCode9/", note = "Study")
        val calls = mutableListOf<LaunchCall>()

        val opener = ReelOpener(
            loadReelById = { reel },
            getOpenMethod = { OpenMethod.IN_APP_PLAYER },
            launcher = object : ReelLaunchDelegate {
                override fun launch(method: OpenMethod, reelId: Long, shortcode: String, uri: String): Boolean {
                    calls.add(LaunchCall(method, reelId, shortcode, uri))
                    return true
                }
            },
            logger = {}
        )

        val result = opener.open(9L)
        assertTrue(result is OpenResult.Opened)
        assertEquals("https://www.instagram.com/reel/EmbedCode9/embed/", calls[0].uri)
    }
}
