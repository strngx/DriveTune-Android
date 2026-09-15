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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.AccentText
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.ui.components.PlaylistCard
import com.drivetune.app.ui.components.PrimaryButton
import com.drivetune.app.ui.components.TrackRow
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.graphics.Color
import com.drivetune.app.ui.components.AddToPlaylistDialog
import com.drivetune.app.ui.components.ConfirmDeleteDialog
import com.drivetune.app.ui.components.RenamePlaylistDialog
import com.drivetune.app.ui.components.TrackInfoDialog
import com.drivetune.app.viewmodel.MainUiState

@Composable
fun PlaylistsScreen(
    state: MainUiState,
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

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
                Column {
                    Text(
                        text = "Your Playlists",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Offline Mixes & Custom Sets",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMint, contentColor = AccentText),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2-column Grid of Playlist Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.playlists.chunked(2).forEach { rowPlaylists ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowPlaylists.forEach { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPlaylists.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = BgSurface2,
            title = { Text("Create New Playlist", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Playlists are stored locally on your device for offline listening.", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onCreatePlaylist(newTitle.trim(), newDesc.trim())
                            newTitle = ""
                            newDesc = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMint, contentColor = AccentText)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    state: MainUiState,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayAllClick: (Playlist) -> Unit,
    onRenamePlaylist: (playlistId: String, newTitle: String) -> Unit = { _, _ -> },
    onDeletePlaylist: (playlistId: String) -> Unit = {},
    onAddTrackToPlaylist: (playlistId: String, trackId: String) -> Unit = { _, _ -> },
    onCreatePlaylist: (title: String, description: String) -> Unit = { _, _ -> },
    onRemoveTrack: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val plTracks = state.downloadedTracks.filter { playlist.trackIds.contains(it.id) }

    var showPlaylistMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    // Track-level dialog state
    var selectedTrackForInfo by remember { mutableStateOf<Track?>(null) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

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
                    text = "Playlist",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Box {
                    IconButton(
                        onClick = { showPlaylistMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BgSurface2)
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimary)
                    }

                    DropdownMenu(
                        expanded = showPlaylistMenu,
                        onDismissRequest = { showPlaylistMenu = false },
                        modifier = Modifier.background(BgSurface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Playlist", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showPlaylistMenu = false
                                onPlayAllClick(playlist)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename Playlist", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = AccentMint, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showPlaylistMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = Color(0xFFEF4444), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showPlaylistMenu = false
                                showDeletePlaylistConfirm = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = playlist.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "${plTracks.size} tracks",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Play Playlist",
                icon = Icons.Default.PlayArrow,
                onClick = { onPlayAllClick(playlist) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Playlist Tracks",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(plTracks) { track ->
            TrackRow(
                track = track,
                isPlaying = state.isPlaying && state.currentTrack?.id == track.id,
                onClick = { onTrackClick(track) },
                onAddToPlaylistClick = { selectedTrackForPlaylist = track },
                onTrackInfoClick = { selectedTrackForInfo = track },
                onRemoveClick = { trackToDelete = track },
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }

    // Dialogs
    if (showRenameDialog) {
        RenamePlaylistDialog(
            initialTitle = playlist.title,
            onDismiss = { showRenameDialog = false },
            onConfirmRename = { newTitle ->
                showRenameDialog = false
                onRenamePlaylist(playlist.id, newTitle)
            }
        )
    }

    if (showDeletePlaylistConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Playlist?",
            message = "Are you sure you want to delete \"${playlist.title}\"? Downloaded songs will remain in your library.",
            confirmText = "Delete",
            onDismiss = { showDeletePlaylistConfirm = false },
            onConfirm = {
                showDeletePlaylistConfirm = false
                onDeletePlaylist(playlist.id)
            }
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

