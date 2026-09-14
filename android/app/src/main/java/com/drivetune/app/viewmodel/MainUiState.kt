package com.drivetune.app.viewmodel

import com.drivetune.app.auth.AuthState
import com.drivetune.app.auth.AuthenticatedUser
import com.drivetune.app.drive.DriveAudioItem
import com.drivetune.app.drive.DriveBreadcrumb
import com.drivetune.app.drive.DriveFolderItem
import com.drivetune.app.drive.DriveStorageQuota
import com.drivetune.app.model.Album
import com.drivetune.app.model.DownloadItem
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus

enum class ScreenDestination {
    SPLASH,
    ONBOARDING,
    LIBRARY,
    DRIVE,
    PLAYLISTS,
    SETTINGS,
    PRIVACY_POLICY,
    ALBUM_DETAIL,
    PLAYLIST_DETAIL,
    SYNC_QUEUE
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class MainUiState(
    val currentScreen: ScreenDestination = ScreenDestination.SPLASH,
    val authState: AuthState = AuthState.Loading,
    val authenticatedUser: AuthenticatedUser? = null,
    
    // Real Drive API v3 Data
    val driveFolders: List<DriveFolderItem> = emptyList(),
    val driveAudioFiles: List<DriveAudioItem> = emptyList(),
    val driveStorageQuota: DriveStorageQuota? = null,
    val driveBreadcrumbs: List<DriveBreadcrumb> = listOf(DriveBreadcrumb("root", "Your Drive")),
    val isDriveLoading: Boolean = false,
    val isDrivePaginating: Boolean = false,
    val driveError: String? = null,
    val hasNextDrivePage: Boolean = false,
    
    // Real Download State Tracking
    val downloadStatusByFileId: Map<String, TrackDownloadStatus> = emptyMap(),
    val downloadProgressByFileId: Map<String, Int> = emptyMap(),
    val isFolderDownloading: Boolean = false,
    val activeFolderDownloadName: String? = null,
    val folderDownloadCompletedCount: Int = 0,
    val folderDownloadTotalCount: Int = 0,

    // Selected Drive items for future download
    val selectedDriveFileIds: Set<String> = emptySet(),
    
    // Local Offline Library
    val downloadedTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val selectedAlbumId: String? = null,
    val selectedPlaylistId: String? = null,

    // Playback State
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPlaybackSeconds: Int = 0,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isNowPlayingOpen: Boolean = false,
    
    // Simulation / Dev Knobs
    val isOnline: Boolean = true,
    val showEmptyLibrary: Boolean = false,
    val activeToast: String? = null,

    // Add Music & Upload State
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val uploadFileName: String? = null,
    val uploadStatusMessage: String? = null
) {
    val isAuthenticated: Boolean
        get() = authState is AuthState.Authenticated || authenticatedUser != null

    val isDriveEmpty: Boolean
        get() = !isDriveLoading && driveFolders.isEmpty() && driveAudioFiles.isEmpty() && driveError == null

    val currentFolderTitle: String
        get() = driveBreadcrumbs.lastOrNull()?.folderName ?: "Your Drive"

    val isInsideSubfolder: Boolean
        get() = driveBreadcrumbs.size > 1

    val activeDownloadItems: List<DownloadItem>
        get() = downloadedTracks.filter { it.status == TrackDownloadStatus.DOWNLOADING }.map {
            DownloadItem(
                trackId = it.id,
                trackTitle = it.title,
                sizeString = it.sizeString,
                progress = it.downloadProgress
            )
        }

    val offlineStorageSizeFormatted: String
        get() {
            if (showEmptyLibrary) return "0 MB"
            var totalMb = 0.0
            downloadedTracks.filter { it.status == TrackDownloadStatus.DOWNLOADED }.forEach { t ->
                val mb = t.sizeString.replace(" MB", "").toDoubleOrNull() ?: 0.0
                totalMb += mb
            }
            return if (totalMb > 1024) "%.1f GB".format(totalMb / 1024.0) else "%d MB".format(totalMb.toInt())
        }
}
