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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.model.Album
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface4
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.ui.components.PrimaryButton
import com.drivetune.app.ui.components.SecondaryButton
import com.drivetune.app.ui.components.TrackRow
import com.drivetune.app.viewmodel.MainUiState

import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.drivetune.app.ui.components.AddToPlaylistDialog
import com.drivetune.app.ui.components.ConfirmDeleteDialog
import com.drivetune.app.ui.components.TrackInfoDialog
import com.drivetune.app.ui.components.AddToPlaylistDialog
import com.drivetune.app.ui.components.ConfirmDeleteDialog
import com.drivetune.app.ui.components.TrackInfoDialog

@Composable
fun AlbumDetailScreen(
    album: Album,
    state: MainUiState,
    onBackClick: () -> Unit,
    onPlayTrackClick: (Track) -> Unit,
    onPlayAllClick: (Album) -> Unit,
    onDownloadAllClick: (Album) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, trackId: String) -> Unit = { _, _ -> },
    onAddAlbumToPlaylist: (playlistId: String, albumId: String) -> Unit = { _, _ -> },
    onCreatePlaylist: (title: String, description: String) -> Unit = { _, _ -> },
    onRemoveTrack: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val albumTracks = state.downloadedTracks.filter { album.trackIds.contains(it.id) }
    val isAllDownloaded = albumTracks.isNotEmpty() && albumTracks.all { it.status == TrackDownloadStatus.DOWNLOADED }

    var showAlbumMenu by remember { mutableStateOf(false) }
    var showAddAlbumToPlaylistDialog by remember { mutableStateOf(false) }

    // Track-level dialog state
    var selectedTrackForInfo by remember { mutableStateOf<Track?>(null) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
    ) {
        // Top Bar
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Album",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Box {
                    IconButton(
                        onClick = { showAlbumMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BgSurface2)
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimary)
                    }

                    DropdownMenu(
                        expanded = showAlbumMenu,
                        onDismissRequest = { showAlbumMenu = false },
                        modifier = Modifier.background(BgSurface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Album", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showAlbumMenu = false
                                onPlayAllClick(album)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Album to Playlist", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showAlbumMenu = false
                                showAddAlbumToPlaylistDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Album Artwork Hero
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgSurface4)
                        .border(1.dp, BorderMedium, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (album.coverArtUrl.isNotEmpty()) {
                        AsyncImage(
                            model = album.coverArtUrl,
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = album.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = album.artist,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${albumTracks.size} tracks",
                    fontSize = 12.sp,
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PrimaryButton(
                        text = "Play Album",
                        icon = Icons.Default.PlayArrow,
                        onClick = { onPlayAllClick(album) },
                        modifier = Modifier.weight(1f)
                    )

                    if (!isAllDownloaded) {
                        Spacer(modifier = Modifier.width(10.dp))
                        SecondaryButton(
                            text = "Download All",
                            icon = Icons.Default.CloudDownload,
                            onClick = { onDownloadAllClick(album) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Tracklist",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Track rows
        items(albumTracks) { track ->
            TrackRow(
                track = track,
                isPlaying = state.isPlaying && state.currentTrack?.id == track.id,
                onClick = { onPlayTrackClick(track) },
                onAddToPlaylistClick = { selectedTrackForPlaylist = track },
                onTrackInfoClick = { selectedTrackForInfo = track },
                onRemoveClick = { trackToDelete = track },
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }

    // Dialogs
    if (showAddAlbumToPlaylistDialog) {
        AddToPlaylistDialog(
            trackId = album.id,
            playlists = state.playlists,
            onSelectPlaylist = { playlistId ->
                onAddAlbumToPlaylist(playlistId, album.id)
            },
            onCreateNewPlaylist = { title ->
                onCreatePlaylist(title, "")
                onAddAlbumToPlaylist("pl-${System.currentTimeMillis()}", album.id)
            },
            onDismiss = { showAddAlbumToPlaylistDialog = false }
        )
    }

    val trackForPlaylist = selectedTrackForPlaylist
    if (trackForPlaylist != null) {
        AddToPlaylistDialog(
            trackId = trackForPlaylist.id,
            playlists = state.playlists,
            onSelectPlaylist = { playlistId ->
                onAddTrackToPlaylist(playlistId, trackForPlaylist.id)
                selectedTrackForPlaylist = null
            },
            onCreateNewPlaylist = { title ->
                onCreatePlaylist(title, "")
                onAddTrackToPlaylist("pl-${System.currentTimeMillis()}", trackForPlaylist.id)
                selectedTrackForPlaylist = null
            },
            onDismiss = { selectedTrackForPlaylist = null }
        )
    }

    val trackForInfo = selectedTrackForInfo
    if (trackForInfo != null) {
        TrackInfoDialog(
            track = trackForInfo,
            onDismiss = { selectedTrackForInfo = null }
        )
    }

    val deleteTrack = trackToDelete
    if (deleteTrack != null) {
        ConfirmDeleteDialog(
            title = "Remove from Library?",
            message = "This will remove \"${deleteTrack.title}\" from your offline library and delete the downloaded file from device storage.",
            confirmText = "Remove",
            onDismiss = { trackToDelete = null },
            onConfirm = {
                onRemoveTrack(deleteTrack.id)
                trackToDelete = null
            }
        )
    }
}

