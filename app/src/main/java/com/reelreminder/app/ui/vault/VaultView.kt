package com.reelreminder.app.ui.vault

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.R
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.Reel
import com.reelreminder.app.ui.components.ReelPoster
import com.reelreminder.app.ui.components.formatPosterTime
import com.reelreminder.app.ui.components.glass
import com.reelreminder.app.ui.theme.DarkChip
import com.reelreminder.app.ui.theme.DarkEdge
import com.reelreminder.app.ui.theme.DarkGlass
import com.reelreminder.app.ui.theme.DarkInk
import com.reelreminder.app.ui.theme.DarkMute
import com.reelreminder.app.ui.theme.LightChip
import com.reelreminder.app.ui.theme.LightEdge
import com.reelreminder.app.ui.theme.LightGlass
import com.reelreminder.app.ui.theme.LightInk
import com.reelreminder.app.ui.theme.LightMute
import com.reelreminder.app.ui.theme.LocalAppDarkTheme
import com.reelreminder.app.ui.theme.TealPrimary
import com.reelreminder.app.ui.theme.parseColorPair

@Composable
fun VaultView(
    categories: List<Category>,
    reels: List<Reel>,
    selectedCategory: Category?,
    onSelectCategory: (Category?) -> Unit,
    onAddCategory: (String) -> Unit,
    onPlayReel: (Reel) -> Unit,
    onDeleteReel: (Reel) -> Unit,
    onEditReel: (Reel) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    if (selectedCategory != null) {
        CategoryDetailView(
            category = selectedCategory,
            reels = reels.filter { it.categoryId == selectedCategory.id },
            onBack = { onSelectCategory(null) },
            onPlayReel = onPlayReel,
            onDeleteReel = onDeleteReel,
            onEditReel = onEditReel,
            modifier = modifier
        )
    } else {
        VaultCollectionsList(
            categories = categories,
            reels = reels,
            onSelectCategory = onSelectCategory,
            onOpenNewCategory = { showNewCategoryDialog = true },
            modifier = modifier
        )
    }

    if (showNewCategoryDialog) {
        var nameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.new_collection),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text(stringResource(R.string.collection_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddCategory(trimmed)
                            showNewCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun VaultCollectionsList(
    categories: List<Category>,
    reels: List<Reel>,
    onSelectCategory: (Category) -> Unit,
    onOpenNewCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalAppDarkTheme.current
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .padding(bottom = 160.dp)
    ) {
        Text(
            text = stringResource(R.string.tab_vault),
            color = inkColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 38.sp,
            letterSpacing = (-0.03).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.vault_subtitle),
            color = muteColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(18.dp))

        // Category rows
        categories.forEach { category ->
            val catReels = reels.filter { it.categoryId == category.id }
            val nextReel = catReels.filter { !it.done && it.nextTriggerAt > 0L }
                .minByOrNull { it.nextTriggerAt }

            CategoryGlassRow(
                category = category,
                reelsCount = catReels.size,
                nextTime = nextReel?.time,
                isDark = isDark,
                onClick = { onSelectCategory(category) }
            )
            Spacer(Modifier.height(12.dp))
        }

        // Dashed "+ New collection" button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(if (isDark) DarkChip else LightChip)
                .border(2.dp, if (isDark) DarkEdge else LightEdge, RoundedCornerShape(28.dp))
                .clickable(onClick = onOpenNewCategory)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.new_collection),
                color = muteColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryGlassRow(
    category: Category,
    reelsCount: Int,
    nextTime: String?,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute
    val gradientColors = remember(category) {
        parseColorPair(category.gradientStart, category.gradientEnd)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(28.dp), isDark = isDark)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fan of 3 mini posters
            FanOfPosters(
                gradientColors = gradientColors,
                modifier = Modifier
                    .size(width = 70.dp, height = 76.dp)
            )

            Spacer(Modifier.width(16.dp))

            // Category Name + Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    color = inkColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).sp
                )
                Spacer(Modifier.height(3.dp))
                val countText = if (reelsCount == 1) "1 reel" else "$reelsCount reels"
                val nextText = if (nextTime != null) ", next at ${formatPosterTime(nextTime)}" else ""
                Text(
                    text = countText + nextText,
                    color = muteColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = muteColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FanOfPosters(
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Poster 1 (-14deg, offset left)
        Box(
            modifier = Modifier
                .size(46.dp, 62.dp)
                .rotate(-14f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors))
                .border(1.dp, Color(0x8CFFFFFF), shape)
        )
        // Poster 2 (+12deg, offset right)
        Box(
            modifier = Modifier
                .size(46.dp, 62.dp)
                .rotate(12f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors))
                .border(1.dp, Color(0x8CFFFFFF), shape)
        )
        // Poster 3 (center straight)
        Box(
            modifier = Modifier
                .size(46.dp, 62.dp)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Brush.linearGradient(gradientColors))
                .border(1.dp, Color(0x8CFFFFFF), shape)
        )
    }
}

@Composable
private fun CategoryDetailView(
    category: Category,
    reels: List<Reel>,
    onBack: () -> Unit,
    onPlayReel: (Reel) -> Unit,
    onDeleteReel: (Reel) -> Unit,
    onEditReel: (Reel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalAppDarkTheme.current
    val inkColor = if (isDark) DarkInk else LightInk
    val muteColor = if (isDark) DarkMute else LightMute

    val watchedCount = remember(reels) { reels.count { it.done } }
    val reelCount = reels.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .padding(bottom = 160.dp)
    ) {
        // Back Pill "‹ Vault"
        Box(
            modifier = Modifier
                .glass(shape = CircleShape, isDark = isDark)
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to Vault",
                    tint = inkColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.tab_vault),
                    color = inkColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Title & Stats
        Text(
            text = category.name,
            color = inkColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 38.sp,
            letterSpacing = (-0.03).sp
        )
        Spacer(Modifier.height(4.dp))
        val reelsUnit = if (reelCount == 1) "reel" else "reels"
        Text(
            text = "$reelCount $reelsUnit · $watchedCount watched today",
            color = muteColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(20.dp))

        if (reels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(shape = RoundedCornerShape(28.dp), isDark = isDark)
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.collection_empty_title),
                        color = inkColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.collection_empty_body, category.name),
                        color = muteColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 2-column staggered grid (second column 28dp lower!)
            val leftColumn = reels.filterIndexed { index, _ -> index % 2 == 0 }
            val rightColumn = reels.filterIndexed { index, _ -> index % 2 == 1 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Column 1
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    leftColumn.forEach { reel ->
                        ReelPoster(
                            reel = reel,
                            category = category,
                            aspectRatio = 3f / 4f,
                            headlineSize = 18.sp,
                            playButtonSize = 46.dp,
                            canDelete = true,
                            onPlay = { onPlayReel(reel) },
                            onDelete = { onDeleteReel(reel) },
                            onClick = { onEditReel(reel) }
                        )
                    }
                }

                // Column 2 (staggered: top padding 28dp!)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rightColumn.forEach { reel ->
                        ReelPoster(
                            reel = reel,
                            category = category,
                            aspectRatio = 3f / 4f,
                            headlineSize = 18.sp,
                            playButtonSize = 46.dp,
                            canDelete = true,
                            onPlay = { onPlayReel(reel) },
                            onDelete = { onDeleteReel(reel) },
                            onClick = { onEditReel(reel) }
                        )
                    }
                }
            }
        }
    }
}
