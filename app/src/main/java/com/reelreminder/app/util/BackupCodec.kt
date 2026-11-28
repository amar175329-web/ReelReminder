package com.reelreminder.app.util

import com.reelreminder.app.data.Category
import com.reelreminder.app.data.EndType
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.RepeatUnit
import com.reelreminder.app.data.ScheduleType
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val categories: List<Category>,
    val reels: List<Reel>
)

/**
 * Local JSON export/import for user backup (ADR-2, Issue 17). No secrets or tokens.
 */
object BackupCodec {
    fun encode(categories: List<Category>, reels: List<Reel>): String {
        val root = JSONObject()
        root.put("version", 1)

        val catsArr = JSONArray()
        categories.forEach { c ->
            catsArr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("gradientStart", c.gradientStart)
                    put("gradientEnd", c.gradientEnd)
                    put("isDefault", c.isDefault)
                }
            )
        }
        root.put("categories", catsArr)

        val reelsArr = JSONArray()
        reels.forEach { r ->
            reelsArr.put(
                JSONObject().apply {
                    put("url", r.url)
                    put("note", r.note)
                    put("categoryId", r.categoryId)
                    put("scheduleType", r.scheduleType.name)
                    put("time", r.time)
                    put("daysOfWeek", r.daysOfWeek)
                    put("nextTriggerAt", r.nextTriggerAt)
                    put("done", r.done)
                    put("paused", r.paused)
                    put("repeatInterval", r.repeatInterval)
                    put("repeatUnit", r.repeatUnit.name)
                    put("endType", r.endType.name)
                    if (r.endDateIso != null) put("endDateIso", r.endDateIso)
                    if (r.endAfterCount != null) put("endAfterCount", r.endAfterCount)
                }
            )
        }
        root.put("reels", reelsArr)
        return root.toString(2)
    }

    fun decode(json: String): BackupPayload? {
        return runCatching {
            val root = JSONObject(json)
            val cats = mutableListOf<Category>()
            val catsArr = root.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until catsArr.length()) {
                val o = catsArr.getJSONObject(i)
                cats.add(
                    Category(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        gradientStart = o.optString("gradientStart", "#0d9488"),
                        gradientEnd = o.optString("gradientEnd", "#134e4a"),
                        isDefault = o.optBoolean("isDefault", false)
                    )
                )
            }

            val reels = mutableListOf<Reel>()
            val reelsArr = root.optJSONArray("reels") ?: JSONArray()
            for (i in 0 until reelsArr.length()) {
                val o = reelsArr.getJSONObject(i)
                reels.add(
                    Reel(
                        url = o.optString("url", ""),
                        note = o.optString("note", ""),
                        categoryId = o.optLong("categoryId", 1L),
                        scheduleType = ScheduleType.entries.firstOrNull {
                            it.name == o.optString("scheduleType")
                        } ?: ScheduleType.ONCE,
                        time = o.optString("time", "09:00"),
                        daysOfWeek = o.optString("daysOfWeek", ""),
                        nextTriggerAt = o.optLong("nextTriggerAt", 0L),
                        done = o.optBoolean("done", false),
                        paused = o.optBoolean("paused", false),
                        repeatInterval = o.optInt("repeatInterval", 1).coerceAtLeast(1),
                        repeatUnit = RepeatUnit.entries.firstOrNull {
                            it.name == o.optString("repeatUnit")
                        } ?: RepeatUnit.DAYS,
                        endType = EndType.entries.firstOrNull {
                            it.name == o.optString("endType")
                        } ?: EndType.NEVER,
                        endDateIso = o.optString("endDateIso", "").ifBlank { null },
                        endAfterCount = if (o.has("endAfterCount")) o.optInt("endAfterCount") else null
                    )
                )
            }
            BackupPayload(categories = cats, reels = reels)
        }.getOrNull()
    }
}
