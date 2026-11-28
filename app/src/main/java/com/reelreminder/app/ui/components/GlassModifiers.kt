package com.reelreminder.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reelreminder.app.ui.theme.DarkEdge
import com.reelreminder.app.ui.theme.DarkGlass
import com.reelreminder.app.ui.theme.LightEdge
import com.reelreminder.app.ui.theme.LightGlass

/**
 * White glass surface matching prototype/index.html without blurring child text.
 */
fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    @Suppress("UNUSED_PARAMETER") blurRadius: Dp = 26.dp,
    isDark: Boolean = false
): Modifier = this
    .clip(shape)
    .background(
        color = if (isDark) DarkGlass else LightGlass,
        shape = shape
    )
    .border(
        width = 1.dp,
        color = if (isDark) DarkEdge else LightEdge,
        shape = shape
    )
