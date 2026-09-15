package com.drivetune.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivetune.app.drive.DriveAudioItem
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderHighlight
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.DriveBlue
import com.drivetune.app.theme.StatusWarning
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary

import androidx.compose.material.icons.filled.CheckCircle
import com.drivetune.app.model.TrackDownloadStatus

@Composable
fun DriveAudioRow(
    item: DriveAudioItem,
    isSelected: Boolean,
    downloadStatus: TrackDownloadStatus = TrackDownloadStatus.CLOUD,
    downloadProgress: Int = 0,
    onToggleSelect: (Boolean) -> Unit,
    onSaveOffline: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rowBg = if (isSelected) AccentMintDim else Color.Transparent
    val rowBorder = if (isSelected) BorderHighlight else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggleSelect,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentMint,
                uncheckedColor = TextTertiary,
                checkmarkColor = Color(0xFF051A12)
            ),
            modifier = Modifier.padding(end = 6.dp)
        )

        // Audio Icon Thumbnail
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface3),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (downloadStatus == TrackDownloadStatus.DOWNLOADED) AccentMint else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name, Format & Size
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BgSurface3)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.formatExtension,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (item.isShared) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentMintDim)
                            .border(1.dp, AccentMint.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SHARED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentMint
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.sizeFormatted,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (item.modifiedTime.isNotBlank()) {
                    Text(text = " • ", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        text = item.modifiedTime.take(10),
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Real Download State Action Badge
        when (downloadStatus) {
            TrackDownloadStatus.DOWNLOADED -> {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentMintDim)
                        .border(1.dp, AccentMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Saved Offline",
                        tint = AccentMint,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Saved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentMint
                    )
                }
            }
            TrackDownloadStatus.DOWNLOADING -> {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface3)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AccentMint,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (downloadProgress > 0) "$downloadProgress%" else "Saving...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentMint
                    )
                }
            }
            TrackDownloadStatus.FAILED -> {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x26F87171))
                        .border(1.dp, Color(0x59F87171), RoundedCornerShape(8.dp))
                        .clickable(onClick = onSaveOffline)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Download",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retry",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171)
                    )
                }
            }
            TrackDownloadStatus.CLOUD -> {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentMint else BgSurface3)
                        .border(1.dp, if (isSelected) AccentMint else BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable(onClick = onSaveOffline)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Save Offline",
                        tint = if (isSelected) Color(0xFF051A12) else AccentMint,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelected) "Selected" else "Save Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF051A12) else AccentMint
                    )
                }
            }
        }
    }
}

@Composable
fun DriveLoadingView(
    message: String = "Connecting to Google Drive...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = AccentMint,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DriveErrorView(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface2)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFF87171),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Couldn't load Google Drive",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "Try Again",
            icon = Icons.Default.Refresh,
            onClick = onRetryClick
        )
    }
}

@Composable
fun DriveEmptyView(
    isSubfolder: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(BgSurface2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSubfolder) Icons.Default.FolderOpen else Icons.Default.CloudQueue,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isSubfolder) "This folder has no audio files" else "No music found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isSubfolder) "Open another folder to browse its music." else "Add audio files (.mp3, .flac, .m4a, .wav) to Google Drive and refresh.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
