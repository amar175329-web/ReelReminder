package com.reelreminder.app.ui.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.R
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.Reel
import com.reelreminder.app.notifications.NotificationHelper
import com.reelreminder.app.ui.components.PermissionBanner
import com.reelreminder.app.ui.components.ReelPoster
import com.reelreminder.app.ui.components.formatPosterRepeat
import com.reelreminder.app.ui.components.formatPosterTime
import com.reelreminder.app.ui.components.glass
import com.reelreminder.app.ui.theme.AquaBlob
import com.reelreminder.app.ui.theme.AquaGlow
import com.reelreminder.app.ui.theme.DarkChip
import com.reelreminder.app.ui.theme.DarkInk
import com.reelreminder.app.ui.theme.DarkMute
import com.reelreminder.app.ui.theme.LightChip
import com.reelreminder.app.ui.theme.LightInk
import com.reelreminder.app.ui.theme.LightMute
import com.reelreminder.app.ui.theme.SkyBlob
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.ui.theme.TealPrimary
import com.reelreminder.app.ui.theme.parseColorPair
import java.util.Calendar

@Composable
fun TodayView(
    reels: List<Reel>,
    categories: List<Category>,
    clipboardUrl: String?,
    onDismissClipboard: () -> Unit,
    onSaveClipboard: (String) -> Unit,
    onPlayReel: (Reel) -> Unit,
    onToggleDone: (Reel) -> Unit,
    onEditReel: (Reel) -> Unit,
    onOpenAdd: () -> Unit,
    exactAlarmsEnabled: Boolean,
    notificationsEnabled: Boolean,
    onRequestExactAlarms: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute
    val chipColor = if (isDark) DarkChip else LightChip

    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val pendingReels = remember(reels) {
        reels.filter { !it.done && !it.paused }
            .sortedBy { it.nextTriggerAt }
    }
    val watchedCount = remember(reels) { reels.count { it.done } }
    val totalCount = reels.size

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val carouselPosterWidth = screenWidth * 0.78f

    Box(modifier = modifier.fillMaxSize()) {
        // Soft blurred background blobs from prototype
        CanvasBlobs(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In-app header uses assets/logo.png at 42dp, 12dp corners
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x660A7F86), spotColor = Color(0x660A7F86))
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    color = inkColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).sp,
                    modifier = Modifier.weight(1f)
                )

                // Streak Chip
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(chipColor)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${maxOf(watchedCount, 1)} days",
                            color = inkColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Permissions banners
            if (!notificationsEnabled) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                    PermissionBanner(
                        title = stringResource(R.string.notifications_off_title),
                        body = stringResource(R.string.notifications_off_body),
                        actionLabel = stringResource(R.string.enable),
                        onAction = onOpenNotificationSettings
                    )
                }
            }
            if (!exactAlarmsEnabled) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                    PermissionBanner(
                        title = stringResource(R.string.exact_alarms_off_title),
                        body = stringResource(R.string.exact_alarms_off_body),
                        actionLabel = stringResource(R.string.allow),
                        onAction = onRequestExactAlarms
                    )
                }
            }

            // Clipboard fallback alert
            if (clipboardUrl != null) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    ClipboardBanner(
                        url = clipboardUrl,
                        onSave = { onSaveClipboard(clipboardUrl) },
                        onDismiss = onDismissClipboard
                    )
                }
            }

            // Greeting
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                Text(
                    text = greeting,
                    color = inkColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.03).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.today_subtitle),
                    color = muteColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // In-app tip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(chipColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.in_app_tip),
                    color = muteColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            // Snap Carousel of upcoming posters
            if (pendingReels.isNotEmpty()) {
                val lazyListState = rememberLazyListState()
                LazyRow(
                    state = lazyListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(pendingReels, key = { _, r -> r.id }) { index, reel ->
                        ReelPoster(
                            reel = reel,
                            category = categoryMap[reel.categoryId],
                            modifier = Modifier.width(carouselPosterWidth),
                            aspectRatio = 4f / 5f,
                            headlineSize = 28.sp,
                            playButtonSize = 58.dp,
                            showCountdown = index == 0,
                            onPlay = { onPlayReel(reel) },
                            onClick = { onEditReel(reel) }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // "Today's plan" Glass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .glass(shape = RoundedCornerShape(30.dp), isDark = isDark)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = stringResource(R.string.todays_plan),
                            color = inkColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.02).sp
                        )
                        Text(
                            text = stringResource(R.string.watched_count, watchedCount, totalCount),
                            color = muteColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Progress bar
                    val progressFraction = if (totalCount > 0) watchedCount.toFloat() / totalCount else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(chipColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF14B8A6), Color(0xFF22D3EE))
                                    )
                                )
                                .shadow(8.dp, RoundedCornerShape(9.dp), ambientColor = AquaGlow, spotColor = AquaGlow)
                        )
                    }

                    if (reels.isEmpty()) {
                        // Empty state inside card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = muteColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.empty_title),
                                color = inkColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.empty_body),
                                color = muteColor,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onOpenAdd,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(stringResource(R.string.empty_cta))
                            }
                        }
                    } else {
                        // Vertical timeline
                        reels.forEach { reel ->
                            val cat = categoryMap[reel.categoryId]
                            val catColors = remember(cat) {
                                if (cat != null) parseColorPair(cat.gradientStart, cat.gradientEnd)
                                else listOf(TealPrimary, TealDark)
                            }
                            TimelineItemRow(
                                reel = reel,
                                category = cat,
                                categoryColors = catColors,
                                isDark = isDark,
                                onToggleDone = { onToggleDone(reel) },
                                onPlay = { onPlayReel(reel) },
                                onEdit = { onEditReel(reel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    reel: Reel,
    category: Category?,
    categoryColors: List<Color>,
    isDark: Boolean,
    onToggleDone: () -> Unit,
    onPlay: () -> Unit,
    onEdit: () -> Unit
) {
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute
    val chipColor = if (isDark) DarkChip else LightChip

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time
        Text(
            text = formatPosterTime(reel.time),
            color = muteColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(62.dp)
        )

        // Rail dot
        Box(
            modifier = Modifier
                .width(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(if (reel.done) TealPrimary else chipColor)
                    .border(2.dp, TealPrimary, CircleShape)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Note and Category tag
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reel.note.ifBlank { "Saved reel" },
                color = if (reel.done) muteColor else inkColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (reel.done) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(categoryColors))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${category?.name ?: "Reel"}, ${formatPosterRepeat(reel)}",
                    color = muteColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Check and Play buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Check button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (reel.done) TealPrimary else chipColor)
                    .clickable(onClick = onToggleDone),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Mark watched",
                    tint = if (reel.done) Color.White else Color.Transparent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Go / Play button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(6.dp, CircleShape, ambientColor = Color(0x660A7F86), spotColor = Color(0x660A7F86))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0A7F86)))
                    )
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play reel",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ClipboardBanner(
    url: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE6FFFA)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.clipboard_banner_title),
                    color = Color(0xFF042F2E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = url,
                    color = Color(0xFF0D9488),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(stringResource(R.string.clipboard_save))
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF042F2E)
                )
            }
        }
    }
}

@Composable
private fun CanvasBlobs(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        // Aqua blob at top-right
        drawCircle(
            color = AquaBlob,
            radius = 160.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, 0f)
        )
        // Sky blob at bottom-left
        drawCircle(
            color = SkyBlob,
            radius = 150.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(0f, size.height * 0.75f)
        )
    }
}
