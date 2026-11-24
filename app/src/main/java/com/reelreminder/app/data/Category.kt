package com.reelreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val gradientStart: String,
    val gradientEnd: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1L, name = "Morning", gradientStart = "#0891b2", gradientEnd = "#155e75", isDefault = true),
            Category(id = 2L, name = "Motivation", gradientStart = "#0d9488", gradientEnd = "#134e4a", isDefault = true),
            Category(id = 3L, name = "Evening", gradientStart = "#2563eb", gradientEnd = "#1e3a8a", isDefault = true),
            Category(id = 4L, name = "Study", gradientStart = "#059669", gradientEnd = "#064e3b", isDefault = true)
        )

        val COOL_GRADIENT_PAIRS = listOf(
            "#0e7490" to "#164e63",
            "#0284c7" to "#0c4a6e",
            "#16a34a" to "#14532d",
            "#047857" to "#064e3b",
            "#4338ca" to "#3730a3",
            "#0f766e" to "#115e59"
        )

        fun nextGradientPair(existingCount: Int): Pair<String, String> {
            return COOL_GRADIENT_PAIRS[existingCount % COOL_GRADIENT_PAIRS.size]
        }
    }
}
