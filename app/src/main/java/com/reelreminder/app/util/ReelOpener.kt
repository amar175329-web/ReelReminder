package com.reelreminder.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.reelreminder.app.data.Reel

enum class OpenMethod(val label: String, val description: String) {
    M1_DEEP_LINK(
        label = "M1: Instagram deep link",
        description = "instagram://media?id=<mediaId> (undocumented, test on device)"
    ),
    M2_INSTAGRAM_HTTPS(
        label = "M2: Instagram app (HTTPS)",
        description = "ACTION_VIEW canonical HTTPS URL in com.instagram.android"
    ),
    M3_BROWSER(
        label = "M3: Browser (HTTPS)",
        description = "ACTION_VIEW canonical HTTPS URL in browser"
    ),
    IN_APP_PLAYER(
        label = "In-app Player",
        description = "Safe WebView embed + Reel ID + Open in Instagram button"
    )
}

sealed class OpenResult {
    data class Opened(
        val reelId: Long,
        val shortcode: String,
        val uri: String,
        val method: OpenMethod
    ) : OpenResult()

    data class MissingLink(val reelId: Long) : OpenResult()
    data class NotFound(val reelId: Long) : OpenResult()
    data class LaunchFailed(val reelId: Long, val shortcode: String, val uri: String) : OpenResult()
}

interface ReelLaunchDelegate {
    fun launch(method: OpenMethod, reelId: Long, shortcode: String, uri: String): Boolean
}

/**
 * Single entry point for opening any saved reel from Today, Vault, Collections, or Notifications.
 * Always loads the reel from Room BY ID at tap time and logs only id, shortcode, method, and uri.
 */
class ReelOpener(
    private val loadReelById: suspend (Long) -> Reel?,
    private val getOpenMethod: () -> OpenMethod,
    private val launcher: ReelLaunchDelegate,
    private val logger: (String) -> Unit = { msg -> Log.i(TAG, msg) }
) {
    suspend fun open(reelId: Long, overrideMethod: OpenMethod? = null): OpenResult {
        val reel = loadReelById(reelId) ?: return OpenResult.NotFound(reelId)
        val parsed = ShortcodeParser.parse(reel.url)
        if (parsed == null) {
            logger("ReelOpener: id=$reelId shortcode=NONE uri=NONE (missing link)")
            return OpenResult.MissingLink(reelId)
        }

        val method = overrideMethod ?: getOpenMethod()
        val targetUri = buildUri(method, parsed)
        logger("ReelOpener: id=$reelId shortcode=${parsed.shortcode} method=${method.name} uri=$targetUri")

        val launched = launcher.launch(
            method = method,
            reelId = reelId,
            shortcode = parsed.shortcode,
            uri = targetUri
        )
        return if (launched) {
            OpenResult.Opened(
                reelId = reelId,
                shortcode = parsed.shortcode,
                uri = targetUri,
                method = method
            )
        } else {
            OpenResult.LaunchFailed(reelId, parsed.shortcode, targetUri)
        }
    }

    private fun buildUri(method: OpenMethod, parsed: ParsedInstagramReel): String {
        return when (method) {
            OpenMethod.M1_DEEP_LINK -> {
                val mediaId = MediaIdCodec.shortcodeToMediaId(parsed.shortcode)
                if (mediaId != null) {
                    "instagram://media?id=$mediaId"
                } else {
                    parsed.canonicalUrl
                }
            }
            OpenMethod.M2_INSTAGRAM_HTTPS -> parsed.canonicalUrl
            OpenMethod.M3_BROWSER -> parsed.canonicalUrl
            OpenMethod.IN_APP_PLAYER -> "https://www.instagram.com/reel/${parsed.shortcode}/embed/"
        }
    }

    companion object {
        private const val TAG = "ReelOpener"
    }
}

object DebugPrefs {
    private const val PREFS_NAME = "reelreminder_debug"
    private const val KEY_OPEN_METHOD = "open_method"

    fun getOpenMethod(context: Context): OpenMethod {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_OPEN_METHOD, OpenMethod.M2_INSTAGRAM_HTTPS.name)
        return OpenMethod.entries.firstOrNull { it.name == saved } ?: OpenMethod.M2_INSTAGRAM_HTTPS
    }

    fun setOpenMethod(context: Context, method: OpenMethod) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_OPEN_METHOD, method.name)
            .apply()
    }
}

class AndroidReelLaunchDelegate(
    private val context: Context,
    private val onOpenInAppPlayer: (Long) -> Unit
) : ReelLaunchDelegate {

    override fun launch(method: OpenMethod, reelId: Long, shortcode: String, uri: String): Boolean {
        return when (method) {
            OpenMethod.IN_APP_PLAYER -> {
                onOpenInAppPlayer(reelId)
                true
            }
            OpenMethod.M1_DEEP_LINK -> launchM1(uri, shortcode)
            OpenMethod.M2_INSTAGRAM_HTTPS -> launchInstagramHttps(uri)
            OpenMethod.M3_BROWSER -> launchBrowser(uri)
        }
    }

    private fun launchM1(uri: String, shortcode: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(INSTAGRAM_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            launchInstagramHttps("https://www.instagram.com/reel/$shortcode/")
        }
    }

    private fun launchInstagramHttps(uri: String): Boolean {
        val parsedUri = Uri.parse(uri)
        if (isInstagramInstalled()) {
            val igIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                setPackage(INSTAGRAM_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(igIntent)
                return true
            } catch (_: ActivityNotFoundException) {
                // Fall back to browser below
            }
        }
        return launchBrowser(uri)
    }

    private fun launchBrowser(uri: String): Boolean {
        val parsedUri = Uri.parse(uri)
        val browserProbe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        val browserPackage = resolveBrowserPackage(browserProbe)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            if (browserPackage != null) {
                setPackage(browserPackage)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            val fallback = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching {
                context.startActivity(fallback)
                true
            }.getOrDefault(false)
        }
    }

    private fun resolveBrowserPackage(probe: Intent): String? {
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                probe,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return activities
            .mapNotNull { it.activityInfo?.packageName }
            .firstOrNull { it != INSTAGRAM_PACKAGE && it != context.packageName }
    }

    private fun isInstagramInstalled(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    INSTAGRAM_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(INSTAGRAM_PACKAGE, 0)
            }
            true
        }.getOrDefault(false)
    }

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
    }
}
