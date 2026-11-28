package com.reelreminder.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Copies user-picked Photo Picker images into app-internal storage (no storage permission needed).
 */
object CoverStorage {
    private const val DIR_NAME = "covers"

    fun copyFromUri(context: Context, sourceUri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val dest = File(dir, "cover_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            dest.absolutePath
        }.getOrNull()
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    fun deleteAll(context: Context) {
        runCatching {
            File(context.filesDir, DIR_NAME).deleteRecursively()
        }
    }
}
