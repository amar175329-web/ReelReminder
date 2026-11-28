package com.reelreminder.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import com.reelreminder.app.ui.AppViewModel
import com.reelreminder.app.ui.ReelReminderApp
import com.reelreminder.app.ui.theme.LocalAppDarkTheme
import com.reelreminder.app.ui.theme.ReelReminderTheme
import com.reelreminder.app.util.DebugPrefs
import com.reelreminder.app.util.ShareCaptureParser
import com.reelreminder.app.util.ShareDiagnosticsStore

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Compose refreshes the permission banner on the next ON_RESUME.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(DebugPrefs.getThemeMode(this)) }
            ReelReminderTheme(themeMode = themeMode) {
                val isDark = LocalAppDarkTheme.current
                SideEffect {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !isDark
                    controller.isAppearanceLightNavigationBars = !isDark
                }
                ReelReminderApp(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        DebugPrefs.setThemeMode(this, mode)
                    },
                    onRequestNotificationSettings = ::openNotificationSettings,
                    onRequestExactAlarms = ::openExactAlarmSettings
                )
            }
        }

        handleShareIntent(intent)
        maybeRequestNotificationPermission()
        publishSharingShortcut()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkClipboard(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action != Intent.ACTION_SEND) return
        if (intent.type?.startsWith("text/") != true) return

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        val extraKeys = intent.extras?.keySet()?.toList().orEmpty()
        val snapshot = ShareCaptureParser.parse(
            rawText = text,
            mimeType = intent.type,
            extraKeys = extraKeys
        )
        ShareDiagnosticsStore.save(this, snapshot)

        val canonicalUrl = snapshot.extractedUrl
        if (canonicalUrl != null) {
            viewModel.onSharedUrl(canonicalUrl)
        } else {
            Toast.makeText(this, "Only Instagram reel links work", Toast.LENGTH_LONG).show()
        }

        // Do not process the same share twice if the activity is reused.
        intent.removeExtra(Intent.EXTRA_TEXT)
    }

    /**
     * Fix 1a: Sharing Shortcuts so ReelReminder ranks at the top in Instagram share sheet
     */
    private fun publishSharingShortcut() {
        runCatching {
            val shortcut = ShortcutInfoCompat.Builder(this, "share_reel")
                .setShortLabel(getString(R.string.shortcut_share_label))
                .setLongLabel(getString(R.string.shortcut_share_long_label))
                .setIcon(IconCompat.createWithResource(this, R.drawable.app_logo))
                .setIntent(
                    Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .setClass(this, MainActivity::class.java)
                )
                .setCategories(setOf("com.reelreminder.app.category.SHARE_TARGET"))
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ASKED_NOTIFICATIONS, false)) {
            prefs.edit().putBoolean(KEY_ASKED_NOTIFICATIONS, true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        runCatching { startActivity(intent) }
            .onFailure { openAppDetailsSettings() }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:$packageName")
        )
        runCatching { startActivity(intent) }
            .onFailure { openAppDetailsSettings() }
    }

    private fun openAppDetailsSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "reelreminder_prefs"
        private const val KEY_ASKED_NOTIFICATIONS = "asked_notifications"
    }
}
