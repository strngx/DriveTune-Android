package com.drivetune.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BgSurface4
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.DriveBlue
import com.drivetune.app.theme.StatusDownloading
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.ui.components.PrimaryButton
import com.drivetune.app.ui.components.SecondaryButton
import com.drivetune.app.viewmodel.MainUiState

@Composable
fun SyncQueueScreen(
    state: MainUiState,
    onBackClick: () -> Unit,
    onDownloadMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BgSurface2)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "Sync Music",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = { /* sync */ },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BgSurface2)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Drive Connection & Metrics Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null, tint = DriveBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Google Drive Connected", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentMintDim)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "ACTIVE SYNC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentMint)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Metrics Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val inDriveCount = state.driveAudioFiles.size
                    SyncMetricBox("In Drive", "$inDriveCount", TextPrimary, Modifier.weight(1f))
                    SyncMetricBox("Downloaded", "${state.downloadedTracks.size}", AccentMint, Modifier.weight(1f))
                    SyncMetricBox("Device Storage", state.offlineStorageSizeFormatted, TextPrimary, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryButton(
                        text = "Pause All",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Download More",
                        onClick = onDownloadMoreClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Download Queue",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        val queueItems = state.activeDownloadItems
        if (queueItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active downloads. Selected music is stored offline.",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }
            }
        } else {
            items(queueItems) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface2)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.trackTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(text = "${item.progress}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusDownloading)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { item.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = StatusDownloading,
                        trackColor = BgSurface4
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Google Drive • ${item.sizeString}", fontSize = 11.sp, color = TextTertiary)
                        Text(text = item.speed, fontSize = 11.sp, color = TextTertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncMetricBox(
    label: String,
    value: String,
    valColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgSurface3)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = valColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}
