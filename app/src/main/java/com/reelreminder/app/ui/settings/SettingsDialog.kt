package com.reelreminder.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelreminder.app.ui.theme.ThemeMode
import com.reelreminder.app.util.OpenMethod
import com.reelreminder.app.util.ShareDiagnosticSnapshot

@Composable
fun SettingsDialog(
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    selectedMethod: OpenMethod,
    lastShared: ShareDiagnosticSnapshot?,
    onSelectMethod: (OpenMethod) -> Unit,
    onCreateReelWithoutLink: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onDeleteAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Settings • Debug",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectThemeMode(mode) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == themeMode,
                            onClick = { onSelectThemeMode(mode) }
                        )
                        Text(
                            text = mode.label,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    text = "Open method (ADR-3)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OpenMethod.entries.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMethod(method) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = method == selectedMethod,
                            onClick = { onSelectMethod(method) }
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(
                                text = method.label,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = method.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCreateReelWithoutLink,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create reel without link")
                }
                HorizontalDivider()
                Text(
                    text = "Data & Privacy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export JSON", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import JSON", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = onDeleteAllData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Delete all my data",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Last shared (diagnostics)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (lastShared == null) {
                    Text(
                        text = "No share received yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "MIME: ${lastShared.mimeType}\n" +
                            "Link: ${lastShared.extractedUrl ?: "none"}\n" +
                            "Shortcode: ${lastShared.shortcode ?: "none"}\n" +
                            "Stream extra: ${if (lastShared.hasStreamExtra) "YES" else "NO"}\n" +
                            "Extra keys: ${lastShared.extraKeys.joinToString(", ").ifBlank { "none" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
