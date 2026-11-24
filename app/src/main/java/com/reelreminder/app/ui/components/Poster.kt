package com.reelreminder.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.Reel
import com.reelreminder.app.data.ScheduleType
import com.reelreminder.app.data.daySet
import com.reelreminder.app.ui.theme.RedDot
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.ui.theme.parseColorPair
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Design v3 Poster:
 * Every saved reel is a "poster": a portrait dark-teal glass cover carrying the
 * user's own note as headline, glass pills for time/repeat, glowing ring + sparkle
 * motif from the logo, and a round white Play button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReelPoster(
    reel: Reel,
    category: Category?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 3f / 4f,
    cornerRadius: Dp = 30.dp,
    headlineSize: TextUnit = 19.sp,
    playButtonSize: Dp = 46.dp,
    showCountdown: Boolean = false,
    canDelete: Boolean = false,
    onPlay: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val gradientColors = remember(category) {
        if (category != null) {
            parseColorPair(category.gradientStart, category.gradientEnd)
        } else {
            listOf(Color(0xFF0D9488), Color(0xFF134E4A))
        }
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .shadow(16.dp, shape, ambientColor = Color(0x66064646), spotColor = Color(0x66064646))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1200f)
                )
            )
            .border(1.dp, Color(0x59FFFFFF), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        // Background motifs: Sparkle + Glowing Ring from Logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Sparkle 1 (four-pointed star)
            val sp1X = width * 0.78f
            val sp1Y = height * 0.34f
            val sp1Size = 12.dp.toPx()
            drawSparkle(sp1X, sp1Y, sp1Size, Color(0xCCFFFFFF))

            // Sparkle 2 (smaller)
            val sp2X = width * 0.16f
            val sp2Y = height * 0.44f
            val sp2Size = 7.dp.toPx()
            drawSparkle(sp2X, sp2Y, sp2Size, Color(0x99FFFFFF))

            // Glowing ring in bottom-right
            val ringRadius = width * 0.38f
            val ringCenter = Offset(width * 1.1f, height * 1.05f)
            drawCircle(
                color = Color(0x22FFFFFF),
                radius = ringRadius,
                center = ringCenter,
                style = Stroke(width = 16.dp.toPx())
            )
            drawCircle(
                color = Color(0x3367E8F9),
                radius = ringRadius * 0.95f,
                center = ringCenter,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // Poster Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Glass pills (countdown, time, repeat, optional delete)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showCountdown && reel.nextTriggerAt > 0L) {
                    CountdownPill(reel.nextTriggerAt)
                }

                GlassPill(text = formatPosterTime(reel.time))

                GlassPill(text = formatPosterRepeat(reel))

                if (canDelete && onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x38FFFFFF))
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove reel",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Headline (user note)
            val displayNote = reel.note.ifBlank { "Saved reel" }
            Text(
                text = displayNote,
                color = Color.White,
                fontSize = headlineSize,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = headlineSize * 1.15f,
                letterSpacing = (-0.02).sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (onPlay != null) 54.dp else 0.dp)
            )
        }

        // Round white Play button in bottom-right
        if (onPlay != null) {
            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(playButtonSize)
                    .border(3.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play reel",
                        tint = TealDark,
                        modifier = Modifier.size(playButtonSize * 0.48f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x47FFFFFF), CircleShape)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CountdownPill(
    triggerAt: Long,
    modifier: Modifier = Modifier
) {
    val remaining = triggerAt - System.currentTimeMillis()
    val text = when {
        remaining <= 0L -> "now"
        remaining < 60_000L -> "in 1m"
        remaining < 3600_000L -> "in ${remaining / 60_000L}m"
        else -> {
            val hours = remaining / 3600_000L
            val mins = (remaining % 3600_000L) / 60_000L
            if (mins == 0L) "in ${hours}h" else "in ${hours}h ${mins}m"
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x47FFFFFF), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(RedDot)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun formatPosterTime(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val h = parts[0].toIntOrNull() ?: return time
    val m = parts[1]
    val displayH = if (h % 12 == 0) 12 else h % 12
    val amPm = if (h < 12) "am" else "pm"
    return "$displayH:$m $amPm"
}

fun formatPosterRepeat(reel: Reel): String {
    return when (reel.scheduleType) {
        ScheduleType.ONCE -> "Once"
        ScheduleType.DAILY -> "Daily"
        ScheduleType.WEEKLY -> {
            val days = reel.daySet().sorted().joinToString(", ") {
                DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            if (days.isBlank()) "Weekly" else days
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    cx: Float,
    cy: Float,
    size: Float,
    color: Color
) {
    val half = size / 2f
    val quarter = size / 5f
    val path = Path().apply {
        moveTo(cx, cy - half)
        quadraticTo(cx, cy, cx + half, cy)
        quadraticTo(cx, cy, cx, cy + half)
        quadraticTo(cx, cy, cx - half, cy)
        quadraticTo(cx, cy, cx, cy - half)
        close()
    }
    drawPath(path = path, color = color)
}
