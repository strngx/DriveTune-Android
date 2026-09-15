package com.drivetune.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.model.Track
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.AccentMintGlow
import com.drivetune.app.theme.AccentText
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface4
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.viewmodel.MainUiState
import com.drivetune.app.viewmodel.RepeatMode

import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.drivetune.app.ui.components.AddToPlaylistDialog
import com.drivetune.app.ui.components.ConfirmDeleteDialog
import com.drivetune.app.ui.components.TrackInfoDialog

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun NowPlayingModal(
    state: MainUiState,
    onCloseClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Int) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onAddToPlaylist: (playlistId: String, trackId: String) -> Unit = { _, _ -> },
    onCreatePlaylist: (title: String, description: String) -> Unit = { _, _ -> },
    onViewAlbum: (String) -> Unit = {},
    onRemoveFromLibrary: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: return
    var isFavorite by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showTrackInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = state.isNowPlayingOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF172433), BgBase),
                        radius = 1400f
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BgSurface2)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PLAYING FROM OFFLINE STORAGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentMint,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Text(
                        text = track.album,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BgSurface2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(BgSurface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to Playlist", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                showAddToPlaylistDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Album", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Album, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onCloseClick()
                                onViewAlbum(track.albumId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Track Information", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                showTrackInfoDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove from Library", color = Color(0xFFEF4444), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }

            // Center Artwork Hero (270dp x 270dp)
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(BgSurface4)
                    .border(1.dp, BorderMedium, RoundedCornerShape(28.dp))
            ) {
                AsyncImage(
                    model = track.coverArtUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Song Info & Controls Container
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title and Heart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${track.artist} — ${track.album}",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) AccentMint else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrub Bar
                val currentSeconds = state.currentPlaybackSeconds
                val durationSeconds = track.durationSeconds
                val progressFraction = if (durationSeconds > 0) currentSeconds.toFloat() / durationSeconds else 0f

                Slider(
                    value = progressFraction,
                    onValueChange = { frac ->
                        onSeek((frac * durationSeconds).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentMint,
                        activeTrackColor = AccentMint,
                        inactiveTrackColor = BgSurface4
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentSeconds),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextTertiary
                    )
                    Text(
                        text = "-${formatTime((durationSeconds - currentSeconds).coerceAtLeast(0))}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transport Controls Main Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (state.isShuffleEnabled) AccentMint else TextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onPreviousClick, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Large Play/Pause FAB
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(AccentMint)
                            .clickable(onClick = onPlayPauseClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = AccentText,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(onClick = onNextClick, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = onToggleRepeat) {
                        Icon(
                            imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (state.repeatMode != RepeatMode.OFF) AccentMint else TextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Utility Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentMintDim)
                            .border(1.dp, AccentMint.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "OFFLINE • ${track.format} ${track.bitrate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentMint
                        )
                    }

                    Row {
                        IconButton(onClick = { showAddToPlaylistDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to Playlist", tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showTrackInfoDialog) {
        TrackInfoDialog(
            track = track,
            onDismiss = { showTrackInfoDialog = false }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            trackId = track.id,
            playlists = state.playlists,
            onSelectPlaylist = { playlistId ->
                onAddToPlaylist(playlistId, track.id)
            },
            onCreateNewPlaylist = { title ->
                onCreatePlaylist(title, "")
                onAddToPlaylist("pl-${System.currentTimeMillis()}", track.id)
            },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Remove from Library?",
            message = "This will remove \"${track.title}\" from your offline library and delete the downloaded file from device storage.",
            confirmText = "Remove",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onCloseClick()
                onRemoveFromLibrary(track.id)
            }
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

