package com.reelreminder.app.notifications

import android.app.PendingIntent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reelreminder.app.PlayerActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationPendingIntentTest {

    @Test
    fun threeNotificationsHaveDistinctPendingIntents() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intents = listOf(11L, 22L, 33L).map { id ->
            val intent = PlayerActivity.createIntent(context, id)
            val pi = PendingIntent.getActivity(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Triple(id, intent, pi)
        }

        assertEquals("reelreminder://reel/11", intents[0].second.dataString)
        assertEquals("reelreminder://reel/22", intents[1].second.dataString)
        assertEquals("reelreminder://reel/33", intents[2].second.dataString)

        assertNotEquals(intents[0].third, intents[1].third)
        assertNotEquals(intents[1].third, intents[2].third)
        assertNotEquals(intents[0].third, intents[2].third)
    }
}
