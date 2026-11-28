package com.reelreminder.app.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationIntentSpecTest {

    @Test
    fun threeReelsProduceDistinctRequestCodesAndDataUris() {
        val spec1 = NotificationHelper.buildPlayerIntentSpec(101L)
        val spec2 = NotificationHelper.buildPlayerIntentSpec(102L)
        val spec3 = NotificationHelper.buildPlayerIntentSpec(103L)

        assertEquals(101, spec1.requestCode)
        assertEquals(102, spec2.requestCode)
        assertEquals(103, spec3.requestCode)

        assertEquals("reelreminder://reel/101", spec1.dataUri)
        assertEquals("reelreminder://reel/102", spec2.dataUri)
        assertEquals("reelreminder://reel/103", spec3.dataUri)

        assertNotEquals(spec1.dataUri, spec2.dataUri)
        assertNotEquals(spec2.dataUri, spec3.dataUri)
        assertNotEquals(spec1.requestCode, spec3.requestCode)
    }
}
