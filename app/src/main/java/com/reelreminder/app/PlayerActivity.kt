package com.reelreminder.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.reelreminder.app.data.Reel
import com.reelreminder.app.ui.player.PlayerScreen
import com.reelreminder.app.ui.theme.ReelReminderTheme
import com.reelreminder.app.util.AndroidReelLaunchDelegate
import com.reelreminder.app.util.DebugPrefs
import com.reelreminder.app.util.OpenMethod
import com.reelreminder.app.util.ReelOpener
import kotlinx.coroutines.launch

/**
 * Dedicated activity launched by notification taps.
 * Always loads the reel fresh from Room by ID (never from a cached variable).
 */
class PlayerActivity : ComponentActivity() {

    private val currentReelId = mutableStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        currentReelId.value = extractReelId(intent)

        val app = application as ReelReminderApplication

        setContent {
            ReelReminderTheme {
                val reelId = currentReelId.value
                var loadedReel by remember(reelId) { mutableStateOf<Reel?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(reelId) {
                    loadedReel = if (reelId > 0L) app.repository.get(reelId) else null
                }

                val opener = remember(app) {
                    ReelOpener(
                        loadReelById = { id -> app.repository.get(id) },
                        getOpenMethod = { DebugPrefs.getOpenMethod(this@PlayerActivity) },
                        launcher = AndroidReelLaunchDelegate(
                            context = this@PlayerActivity,
                            onOpenInAppPlayer = { /* already in PlayerActivity */ }
                        )
                    )
                }

                PlayerScreen(
                    reel = loadedReel,
                    onBack = { finish() },
                    onOpenInInstagram = {
                        if (reelId > 0L) {
                            scope.launch {
                                opener.open(reelId, overrideMethod = OpenMethod.M2_INSTAGRAM_HTTPS)
                            }
                        }
                    },
                    onOpenInBrowser = {
                        if (reelId > 0L) {
                            scope.launch {
                                opener.open(reelId, overrideMethod = OpenMethod.M3_BROWSER)
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentReelId.value = extractReelId(intent)
    }

    private fun extractReelId(intent: Intent?): Long {
        if (intent == null) return -1L
        val fromExtra = intent.getLongExtra(EXTRA_REEL_ID, -1L)
        if (fromExtra > 0L) return fromExtra
        return intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
    }

    companion object {
        const val EXTRA_REEL_ID = "extra_reel_id"

        fun createIntent(context: Context, reelId: Long): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("reelreminder://reel/$reelId")
                putExtra(EXTRA_REEL_ID, reelId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
