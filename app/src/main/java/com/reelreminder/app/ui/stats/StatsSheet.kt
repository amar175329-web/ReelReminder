package com.reelreminder.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.data.Category
import com.reelreminder.app.data.Reel
import com.reelreminder.app.ui.theme.LightChip
import com.reelreminder.app.ui.theme.TealPrimary
import com.reelreminder.app.util.StreakStats

@Composable
fun StatsDialog(
    streakStats: StreakStats,
    watchedToday: Int,
    totalToday: Int,
    categories: List<Category>,
    reels: List<Reel>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Your Stats",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${streakStats.currentStreakDays} ${if (streakStats.currentStreakDays == 1) "day" else "days"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = TealPrimary
                        )
                        Text(
                            text = "Current streak",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$watchedToday of $totalToday",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Watched today",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Last 7 days",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        streakStats.last7Days.forEach { active ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (active) TealPrimary else LightChip)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "By collection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    categories.forEach { cat ->
                        val count = reels.count { it.categoryId == cat.id }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = cat.name, fontSize = 14.sp)
                            Text(
                                text = "$count",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
