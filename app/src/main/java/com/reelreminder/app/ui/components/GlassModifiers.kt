package com.reelreminder.app.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reelreminder.app.ui.theme.DarkEdge
import com.reelreminder.app.ui.theme.DarkGlass
import com.reelreminder.app.ui.theme.LightEdge
import com.reelreminder.app.ui.theme.LightGlass

/**
 * White glass effect matching Design v3:
 * Light: white 55% + blur 26dp + 1dp white edge + 1dp inner top highlight.
 * Dark: white 7% + 1dp edge.
 * Uses RenderEffect blur on API 31+, translucent solid fallback below.
 */
fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    blurRadius: Dp = 26.dp,
    isDark: Boolean = false
): Modifier = this
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                val radiusPx = blurRadius.toPx()
                renderEffect = RenderEffect
                    .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
        } else {
            Modifier
        }
    )
    .background(
        color = if (isDark) DarkGlass else LightGlass,
        shape = shape
    )
    .border(
        width = 1.dp,
        color = if (isDark) DarkEdge else LightEdge,
        shape = shape
    )
