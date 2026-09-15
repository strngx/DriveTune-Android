package com.drivetune.app

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.ui.components.DriveTuneBottomNav
import com.drivetune.app.ui.components.MiniPlayer
import com.drivetune.app.ui.screens.AlbumDetailScreen
import com.drivetune.app.ui.screens.DriveBrowserScreen
import com.drivetune.app.ui.screens.LibraryScreen
import com.drivetune.app.ui.screens.NowPlayingModal
import com.drivetune.app.ui.screens.OnboardingScreen
import com.drivetune.app.ui.screens.PlaylistDetailScreen
import com.drivetune.app.ui.screens.PlaylistsScreen
import com.drivetune.app.ui.screens.SettingsScreen
import com.drivetune.app.ui.screens.SplashScreen
import com.drivetune.app.ui.screens.SyncQueueScreen
import com.drivetune.app.viewmodel.MainViewModel
import com.drivetune.app.viewmodel.ScreenDestination

import com.drivetune.app.ui.screens.PrivacyPolicyScreen

@Composable
fun DriveTuneApp(
    viewModel: MainViewModel = viewModel(),
    signInLauncher: ActivityResultLauncher<Intent>? = null
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.activeToast) {
        state.activeToast?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.authState) {
        if (state.currentScreen == ScreenDestination.SPLASH) {
            kotlinx.coroutines.delay(800)
            when (state.authState) {
                is com.drivetune.app.auth.AuthState.Authenticated -> viewModel.navigateTo(ScreenDestination.LIBRARY)
                is com.drivetune.app.auth.AuthState.Unauthenticated, is com.drivetune.app.auth.AuthState.Error -> viewModel.navigateTo(ScreenDestination.ONBOARDING)
                com.drivetune.app.auth.AuthState.Loading -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = BgSurface3,
                        contentColor = TextPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .border(1.dp, BorderMedium, RoundedCornerShape(12.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = data.visuals.message,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (state.isAuthenticated && state.currentScreen != ScreenDestination.ONBOARDING && state.currentScreen != ScreenDestination.SPLASH && state.currentScreen != ScreenDestination.PRIVACY_POLICY) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Persistent Mini-Player above bottom nav
                        if (state.currentTrack != null) {
                            val duration = state.currentTrack?.durationSeconds ?: 1
                            val fraction = if (duration > 0) (state.currentPlaybackSeconds.toFloat() / duration).coerceIn(0f, 1f) else 0f
                            MiniPlayer(
                                track = state.currentTrack!!,
                                isPlaying = state.isPlaying,
                                progressPercent = fraction,
                                onClick = { viewModel.openNowPlaying() },
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onNextClick = { viewModel.playNext() }
                            )
                        }

                        DriveTuneBottomNav(
                            currentScreen = state.currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            },
            containerColor = BgBase
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = state.currentScreen,
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        ScreenDestination.SPLASH -> {
                            SplashScreen()
                        }
                        ScreenDestination.ONBOARDING -> {
                            OnboardingScreen(
                                authState = state.authState,
                                onSignInClick = {
                                    val intent = viewModel.getGoogleSignInIntent()
                                    signInLauncher?.launch(intent)
                                }
                            )
                        }
                        ScreenDestination.LIBRARY -> {
                            LibraryScreen(
                                state = state,
                                onTrackClick = { viewModel.playTrack(it) },
                                onAlbumClick = { viewModel.openAlbum(it) },
                                onPlaylistClick = { viewModel.openPlaylist(it) },
                                onPlayAlbumClick = { viewModel.playAlbum(it) },
                                onBrowseDriveClick = { viewModel.navigateTo(ScreenDestination.DRIVE) },
                                onSyncQueueClick = { viewModel.navigateTo(ScreenDestination.DRIVE) },
                                onProfileClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                                onAddMusic = { uris, folderId, folderName -> viewModel.uploadPhoneAudioFiles(uris, folderId, folderName) },
                                onLoadDriveFolders = { parentId -> viewModel.getAvailableDriveFolders(parentId) },
                                onCreateDriveFolder = { name, parentId, onResult -> viewModel.createNewDriveFolder(name, parentId, onResult) },
                                onAddTrackToPlaylist = { plId, tId -> viewModel.addTrackToPlaylist(plId, tId) },
                                onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) },
                                onRemoveTrack = { trackId -> viewModel.removeTrackFromLibrary(trackId) }
                            )
                        }
                        ScreenDestination.DRIVE -> {
                            DriveBrowserScreen(
                                state = state,
                                onFolderClick = { id, name -> viewModel.openDriveFolder(id, name) },
                                onBackToParentFolder = { viewModel.navigateDriveBack() },
                                onRefreshDrive = { viewModel.refreshDrive() },
                                onSaveOfflineFile = { viewModel.saveDriveFileOffline(it) },
                                onDownloadFolder = { viewModel.downloadCurrentFolder() },
                                onToggleSelectFile = { viewModel.toggleDriveFileSelection(it) },
                                onSelectAllFiles = { viewModel.selectAllDriveFiles() },
                                onClearFileSelection = { viewModel.clearDriveFileSelection() },
                                onDownloadSelectedFiles = { viewModel.downloadSelectedDriveFiles() },
                                onLoadNextPage = { viewModel.loadNextDrivePage() },
                                onProfileClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                            )
                        }
                        ScreenDestination.ALBUM_DETAIL -> {
                            val album = state.albums.find { it.id == state.selectedAlbumId } ?: state.albums.firstOrNull()
                            if (album != null) {
                                AlbumDetailScreen(
                                    album = album,
                                    state = state,
                                    onBackClick = { viewModel.navigateTo(ScreenDestination.LIBRARY) },
                                    onPlayTrackClick = { viewModel.playTrack(it) },
                                    onPlayAllClick = { viewModel.playAlbum(it) },
                                    onDownloadAllClick = { /* Already downloaded */ },
                                    onAddTrackToPlaylist = { plId, tId -> viewModel.addTrackToPlaylist(plId, tId) },
                                    onAddAlbumToPlaylist = { plId, aId -> viewModel.addAlbumToPlaylist(plId, aId) },
                                    onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) },
                                    onRemoveTrack = { trackId -> viewModel.removeTrackFromLibrary(trackId) }
                                )
                            }
                        }
                        ScreenDestination.PLAYLISTS -> {
                            PlaylistsScreen(
                                state = state,
                                onPlaylistClick = { viewModel.openPlaylist(it) },
                                onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) }
                            )
                        }
                        ScreenDestination.PLAYLIST_DETAIL -> {
                            val playlist = state.playlists.find { it.id == state.selectedPlaylistId } ?: state.playlists.firstOrNull()
                            if (playlist != null) {
                                PlaylistDetailScreen(
                                    playlist = playlist,
                                    state = state,
                                    onBackClick = { viewModel.navigateTo(ScreenDestination.LIBRARY) },
                                    onTrackClick = { viewModel.playTrack(it) },
                                    onPlayAllClick = { viewModel.playPlaylist(it) },
                                    onRenamePlaylist = { plId, newTitle -> viewModel.renamePlaylist(plId, newTitle) },
                                    onDeletePlaylist = { plId -> viewModel.deletePlaylist(plId) },
                                    onAddTrackToPlaylist = { plId, tId -> viewModel.addTrackToPlaylist(plId, tId) },
                                    onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) },
                                    onRemoveTrack = { trackId -> viewModel.removeTrackFromLibrary(trackId) }
                                )
                            }
                        }
                        ScreenDestination.SYNC_QUEUE -> {
                            SyncQueueScreen(
                                state = state,
                                onBackClick = { viewModel.navigateTo(ScreenDestination.LIBRARY) },
                                onDownloadMoreClick = { viewModel.navigateTo(ScreenDestination.DRIVE) }
                            )
                        }
                        ScreenDestination.SETTINGS -> {
                            SettingsScreen(
                                state = state,
                                onSignOutClick = { viewModel.signOut() },
                                onClearStorageClick = { viewModel.clearDownloadedStorage() },
                                onPrivacyPolicyClick = { viewModel.navigateTo(ScreenDestination.PRIVACY_POLICY) }
                            )
                        }
                        ScreenDestination.PRIVACY_POLICY -> {
                            PrivacyPolicyScreen(
                                onBackClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Now Playing Hero Overlay
        NowPlayingModal(
            state = state,
            onCloseClick = { viewModel.closeNowPlaying() },
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.playNext() },
            onPreviousClick = { viewModel.playPrevious() },
            onSeek = { viewModel.seekTo(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onAddToPlaylist = { plId, tId -> viewModel.addTrackToPlaylist(plId, tId) },
            onCreatePlaylist = { title, desc -> viewModel.createPlaylist(title, desc) },
            onViewAlbum = { albumId -> viewModel.openAlbum(albumId) },
            onRemoveFromLibrary = { trackId -> viewModel.removeTrackFromLibrary(trackId) }
        )
    }
}
