package com.reelreminder.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.R
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
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.ui.theme.TealPrimary

enum class AppTab {
    TODAY,
    VAULT
}

@Composable
fun FloatingGlassNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val pillBg = if (isDark) DarkGlass else LightGlass
    val pillEdge = if (isDark) DarkEdge else LightEdge

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 18.dp, end = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .shadow(16.dp, CircleShape, ambientColor = Color(0x33004040), spotColor = Color(0x33004040))
                .clip(CircleShape)
                .background(pillBg)
                .border(1.dp, pillEdge, CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Today Tab
            NavTabButton(
                label = stringResource(R.string.tab_today),
                icon = Icons.Outlined.Schedule,
                isSelected = currentTab == AppTab.TODAY,
                onClick = { onTabSelected(AppTab.TODAY) },
                modifier = Modifier.weight(1f)
            )

            // Center "+" button (62dp gradient circle with aqua glow)
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(60.dp)
                    .shadow(12.dp, CircleShape, ambientColor = Color(0x6614B8A6), spotColor = Color(0x6614B8A6))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2DD4BF), Color(0xFF0A7F86))
                        )
                    )
                    .border(1.dp, Color(0x80FFFFFF), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_reminder),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Vault Tab
            NavTabButton(
                label = stringResource(R.string.tab_vault),
                icon = Icons.Outlined.GridView,
                isSelected = currentTab == AppTab.VAULT,
                onClick = { onTabSelected(AppTab.VAULT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavTabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val activeBg = if (isDark) DarkChip else LightChip
    val activeColor = TealDark
    val inactiveColor = if (isDark) DarkMute else LightMute

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}
