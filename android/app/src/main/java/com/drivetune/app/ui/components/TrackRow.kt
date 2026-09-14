package com.drivetune.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderHighlight
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.StatusDownloading
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary

import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToPlaylistClick: (() -> Unit)? = null,
    onTrackInfoClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    val rowBackground = if (isPlaying) BgSurface2 else Color.Transparent
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Artwork Thumbnail
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface3),
            contentAlignment = Alignment.Center
        ) {
            if (track.coverArtUrl.isNotEmpty()) {
                AsyncImage(
                    model = track.coverArtUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isPlaying) AccentMint else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerAnimation()
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isPlaying) AccentMint else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (track.format.isNotEmpty()) {
                    Text(text = " • ", fontSize = 10.sp, color = TextTertiary)
                    Text(
                        text = track.format,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (track.durationFormatted != "0:00") {
            Text(
                text = track.durationFormatted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextTertiary
            )
        }

        Box {
            IconButton(
                onClick = {
                    if (onMoreClick != null) {
                        onMoreClick()
                    } else {
                        showMenu = true
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(BgSurface2)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = TextPrimary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                if (onAddToPlaylistClick != null) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylistClick()
                        }
                    )
                }
                if (onTrackInfoClick != null) {
                    DropdownMenuItem(
                        text = { Text("Track Information", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onTrackInfoClick()
                        }
                    )
                }
                if (onRemoveClick != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from Library", color = Color(0xFFEF4444), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onRemoveClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EqualizerAnimation() {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(modifier = Modifier.width(2.5.dp).height(10.dp).background(AccentMint, RoundedCornerShape(1.dp)))
        Spacer(modifier = Modifier.width(2.dp))
        Box(modifier = Modifier.width(2.5.dp).height(16.dp).background(AccentMint, RoundedCornerShape(1.dp)))
        Spacer(modifier = Modifier.width(2.dp))
        Box(modifier = Modifier.width(2.5.dp).height(8.dp).background(AccentMint, RoundedCornerShape(1.dp)))
    }
}
