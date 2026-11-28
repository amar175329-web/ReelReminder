package com.reelreminder.app.util

import android.content.Context

data class ShareDiagnosticSnapshot(
    val mimeType: String,
    val extraKeys: List<String>,
    val extractedUrl: String?,
    val shortcode: String?,
    val hasStreamExtra: Boolean
)

object ShareCaptureParser {
    fun parse(
        rawText: String?,
        mimeType: String?,
        extraKeys: Collection<String>
    ): ShareDiagnosticSnapshot {
        val parsed = ShortcodeParser.extractFromText(rawText)
        val sortedKeys = extraKeys.sorted()
        val hasStream = sortedKeys.any { it.contains("STREAM", ignoreCase = true) }
        return ShareDiagnosticSnapshot(
            mimeType = mimeType ?: "none",
            extraKeys = sortedKeys,
            extractedUrl = parsed?.canonicalUrl,
            shortcode = parsed?.shortcode,
            hasStreamExtra = hasStream
        )
    }
}

object ShareDiagnosticsStore {
    private const val PREFS_NAME = "reelreminder_debug"
    private const val KEY_MIME = "last_share_mime"
    private const val KEY_KEYS = "last_share_keys"
    private const val KEY_URL = "last_share_url"
    private const val KEY_CODE = "last_share_code"
    private const val KEY_STREAM = "last_share_stream"

    fun save(context: Context, snapshot: ShareDiagnosticSnapshot) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MIME, snapshot.mimeType)
            .putString(KEY_KEYS, snapshot.extraKeys.joinToString(", "))
            .putString(KEY_URL, snapshot.extractedUrl.orEmpty())
            .putString(KEY_CODE, snapshot.shortcode.orEmpty())
            .putBoolean(KEY_STREAM, snapshot.hasStreamExtra)
            .apply()
    }

    fun load(context: Context): ShareDiagnosticSnapshot? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mime = prefs.getString(KEY_MIME, null) ?: return null
        val keys = prefs.getString(KEY_KEYS, "").orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val url = prefs.getString(KEY_URL, "").orEmpty().ifBlank { null }
        val code = prefs.getString(KEY_CODE, "").orEmpty().ifBlank { null }
        val hasStream = prefs.getBoolean(KEY_STREAM, false)
        return ShareDiagnosticSnapshot(
            mimeType = mime,
            extraKeys = keys,
            extractedUrl = url,
            shortcode = code,
            hasStreamExtra = hasStream
        )
    }
}
