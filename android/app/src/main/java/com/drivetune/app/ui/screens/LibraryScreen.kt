package com.drivetune.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.model.Album
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface1
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.ui.components.AlbumCard
import com.drivetune.app.ui.components.OfflineBanner
import com.drivetune.app.ui.components.PlaylistCard
import com.drivetune.app.ui.components.PrimaryButton
import com.drivetune.app.ui.components.SecondaryButton
import com.drivetune.app.ui.components.TrackRow
import com.drivetune.app.viewmodel.MainUiState

import com.drivetune.app.drive.DriveFolderItem
import com.drivetune.app.ui.components.AddToPlaylistDialog
import com.drivetune.app.ui.components.ConfirmDeleteDialog
import com.drivetune.app.ui.components.DriveFolderPickerDialog
import com.drivetune.app.ui.components.TrackInfoDialog

@Composable
fun LibraryScreen(
    state: MainUiState,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onPlayAlbumClick: (Album) -> Unit,
    onBrowseDriveClick: () -> Unit,
    onSyncQueueClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddMusic: (List<Uri>, String?, String) -> Unit = { _, _, _ -> },
    onLoadDriveFolders: suspend (String?) -> List<DriveFolderItem> = { emptyList() },
    onCreateDriveFolder: (String, String?, (DriveFolderItem?) -> Unit) -> Unit = { _, _, _ -> },
    onAddTrackToPlaylist: (playlistId: String, trackId: String) -> Unit = { _, _ -> },
    onCreatePlaylist: (title: String, description: String) -> Unit = { _, _ -> },
    onRemoveTrack: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Albums", "Playlists", "All Tracks")

    // Add Music selection state
    var selectedAudioUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showDestinationDialog by remember { mutableStateOf(false) }

    // Track-level dialog state for All Tracks tab
    var selectedTrackForInfo by remember { mutableStateOf<Track?>(null) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    // Audio file picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedAudioUris = uris
            showDestinationDialog = true
        }
    }

    val downloadedTracks = state.downloadedTracks
    val filteredTracks = remember(downloadedTracks, searchQuery) {
        if (searchQuery.isBlank()) downloadedTracks
        else downloadedTracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Library",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Add Music Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentMint.copy(alpha = 0.15f))
                            .border(1.dp, AccentMint.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable {
                                audioPickerLauncher.launch(
                                    arrayOf("audio/*", "application/ogg", "audio/mpeg", "audio/mp4", "audio/flac", "audio/x-wav")
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Music",
                                tint = AccentMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Music",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentMint
                            )
                        }
                    }

                    // Account Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, BorderMedium, CircleShape)
                            .clickable(onClick = onProfileClick)
                    ) {
                        val avatar = state.authenticatedUser?.avatarUrl
                        if (!avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatar,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(BgSurface3),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.authenticatedUser?.displayName?.take(1)?.uppercase() ?: "G",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentMint,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upload in progress banner
            AnimatedVisibility(
                visible = state.isUploading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgSurface2)
                        .border(1.dp, AccentMint.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.uploadFileName ?: "Uploading audio...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = state.uploadStatusMessage ?: "Saving to Google Drive & DriveTune",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "${state.uploadProgress}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentMint
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.uploadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentMint,
                        trackColor = BgSurface3,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Offline status banner if offline
            OfflineBanner(isOffline = !state.isOnline)
            if (!state.isOnline) Spacer(modifier = Modifier.height(14.dp))

            // Search Box (if library has tracks)
            if (downloadedTracks.isNotEmpty()) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search songs, artists, albums...", fontSize = 13.sp, color = TextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface2),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BgSurface3,
                        unfocusedContainerColor = BgSurface2,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Empty state check
        if (downloadedTracks.isEmpty() && !state.isUploading) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(BgSurface2),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your library is empty",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Add music from your phone or download from Google Drive to listen offline.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryButton(
                            text = "Add Music",
                            onClick = {
                                audioPickerLauncher.launch(
                                    arrayOf("audio/*", "application/ogg", "audio/mpeg", "audio/mp4", "audio/flac", "audio/x-wav")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Open Drive",
                            onClick = onBrowseDriveClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            // Tabs Header
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = AccentMint,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AccentMint,
                            height = 2.5.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) TextPrimary else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tab Panels
            when (selectedTabIndex) {
                0 -> { // Albums
                    item {
                        if (state.albums.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No albums yet", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.albums.chunked(2).forEach { rowAlbums ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowAlbums.forEach { album ->
                                            val count = downloadedTracks.count { album.trackIds.contains(it.id) }
                                            AlbumCard(
                                                album = album,
                                                trackCount = count,
                                                isDownloaded = true,
                                                onClick = { onAlbumClick(album.id) },
                                                onPlayClick = { onPlayAlbumClick(album) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowAlbums.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Playlists
                    item {
                        if (state.playlists.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No playlists yet", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
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
                }
                2 -> { // All Tracks
                    if (filteredTracks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No matching tracks found", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(filteredTracks) { track ->
                            TrackRow(
                                track = track,
                                isPlaying = state.isPlaying && state.currentTrack?.id == track.id,
                                onClick = { onTrackClick(track) },
                                onAddToPlaylistClick = { selectedTrackForPlaylist = track },
                                onTrackInfoClick = { selectedTrackForInfo = track },
                                onRemoveClick = { trackToDelete = track },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Real Google Drive Folder Selection Dialog
    if (showDestinationDialog && selectedAudioUris.isNotEmpty()) {
        DriveFolderPickerDialog(
            selectedCount = selectedAudioUris.size,
            onDismiss = {
                showDestinationDialog = false
                selectedAudioUris = emptyList()
            },
            onConfirmDestination = { folderId, folderName ->
                val uris = selectedAudioUris
                showDestinationDialog = false
                selectedAudioUris = emptyList()
                onAddMusic(uris, folderId, folderName)
            },
            onLoadFolders = onLoadDriveFolders,
            onCreateFolder = onCreateDriveFolder
        )
    }

    // Dialogs for All Tracks tab
    selectedTrackForPlaylist?.let { track ->
        AddToPlaylistDialog(
            trackId = track.id,
            playlists = state.playlists,
            onSelectPlaylist = { playlistId ->
                onAddTrackToPlaylist(playlistId, track.id)
                selectedTrackForPlaylist = null
            },
            onCreateNewPlaylist = { title ->
                onCreatePlaylist(title, "")
                onAddTrackToPlaylist("pl-${System.currentTimeMillis()}", track.id)
                selectedTrackForPlaylist = null
            },
            onDismiss = { selectedTrackForPlaylist = null }
        )
    }

    selectedTrackForInfo?.let { track ->
        TrackInfoDialog(
            track = track,
            onDismiss = { selectedTrackForInfo = null }
        )
    }

    trackToDelete?.let { track ->
        ConfirmDeleteDialog(
            title = "Remove from Library?",
            message = "This will remove \"${track.title}\" from your offline library and delete the downloaded file from device storage.",
            confirmText = "Remove",
            onDismiss = { trackToDelete = null },
            onConfirm = {
                onRemoveTrack(track.id)
                trackToDelete = null
            }
        )
    }
}

