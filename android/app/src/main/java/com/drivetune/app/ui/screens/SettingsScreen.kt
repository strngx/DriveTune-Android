package com.drivetune.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentText
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.DriveBlue
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    state: MainUiState,
    onSignOutClick: () -> Unit,
    onClearStorageClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    val user = state.authenticatedUser

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Storage, Playback & Account",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Real Google Account
        item {
            SettingsSectionHeader("Google Account")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!user?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(BgSurface3),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.displayName?.take(1)?.uppercase() ?: "G",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = AccentMint
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user?.displayName ?: "Google User",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = user?.email ?: "Signed in",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DriveBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "Connected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DriveBlue)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSignOutClick)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFFF87171))
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Offline Storage
        item {
            SettingsSectionHeader("Offline Library & Storage")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                SettingsRow(
                    title = "Download Location",
                    subtitle = "App-Private Storage (No broad storage permissions required)",
                    onClick = {}
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

                SettingsRow(
                    title = "Offline Music Storage",
                    subtitle = "${state.offlineStorageSizeFormatted} used on device",
                    onClick = {}
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearDialog = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Clear Downloaded Music", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFFF87171))
                        Text(text = "Free up device storage (files stay safe in Google Drive)", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: About & Privacy
        item {
            SettingsSectionHeader("About & Privacy")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                SettingsRow(
                    title = "DriveTune Version",
                    subtitle = "v1.0.0-prod (Native Android • Kotlin & Compose)",
                    onClick = {}
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))
                SettingsRow(
                    title = "Privacy Policy",
                    subtitle = "Read-only Drive access • Music stays local on device",
                    onClick = onPrivacyPolicyClick
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = BgSurface2,
            title = { Text("Delete All Offline Music?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will remove local audio files from your device to free up storage. Your music remains safe in Google Drive.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearStorageClick()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171), contentColor = Color.White)
                ) {
                    Text("Delete Local Files")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = TextTertiary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
