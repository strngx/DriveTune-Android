package com.drivetune.app.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drivetune.app.auth.AuthManager
import com.drivetune.app.auth.AuthState
import com.drivetune.app.auth.AuthenticatedUser
import com.drivetune.app.data.AppDriveTuneRepository
import com.drivetune.app.data.DriveTuneRepository
import com.drivetune.app.drive.DriveAudioItem
import com.drivetune.app.model.Album
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus
import com.drivetune.app.player.LocalAudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    val authManager: AuthManager = AuthManager(application),
    private val repository: DriveTuneRepository = AppDriveTuneRepository(authManager, application),
    private val audioPlayer: LocalAudioPlayer = LocalAudioPlayer(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var playbackTimerJob: Job? = null

    init {
        // Observe Authentication State
        viewModelScope.launch {
            authManager.authState.collect { auth ->
                _uiState.update { state ->
                    val user = (auth as? AuthState.Authenticated)?.user
                    val nextScreen = when (auth) {
                        is AuthState.Authenticated -> {
                            if (state.currentScreen == ScreenDestination.SPLASH || state.currentScreen == ScreenDestination.ONBOARDING) {
                                ScreenDestination.LIBRARY
                            } else {
                                state.currentScreen
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            ScreenDestination.ONBOARDING
                        }
                        is AuthState.Error -> {
                            ScreenDestination.ONBOARDING
                        }
                        AuthState.Loading -> {
                            if (state.currentScreen == ScreenDestination.SPLASH) ScreenDestination.SPLASH else state.currentScreen
                        }
                    }
                    state.copy(
                        authState = auth,
                        authenticatedUser = user,
                        currentScreen = nextScreen
                    )
                }

                // If authenticated, automatically load root Drive files
                if (auth is AuthState.Authenticated) {
                    loadDriveRoot()
                }
            }
        }

        // Observe Real Drive Data Flows
        viewModelScope.launch {
            repository.realDriveFolders.collect { folders ->
                _uiState.update { it.copy(driveFolders = folders) }
            }
        }
        viewModelScope.launch {
            repository.realDriveAudioFiles.collect { audioFiles ->
                _uiState.update { it.copy(driveAudioFiles = audioFiles) }
            }
        }
        viewModelScope.launch {
            repository.sharedFolders.collect { sharedF ->
                _uiState.update { it.copy(sharedFolders = sharedF) }
            }
        }
        viewModelScope.launch {
            repository.sharedAudioFiles.collect { sharedA ->
                _uiState.update { it.copy(sharedAudioFiles = sharedA) }
            }
        }
        viewModelScope.launch {
            repository.sharedDrives.collect { sharedD ->
                _uiState.update { it.copy(sharedDrives = sharedD) }
            }
        }
        viewModelScope.launch {
            repository.driveStorageQuota.collect { quota ->
                _uiState.update { it.copy(driveStorageQuota = quota) }
            }
        }
        viewModelScope.launch {
            repository.breadcrumbStack.collect { stack ->
                _uiState.update { it.copy(driveBreadcrumbs = stack) }
            }
        }
        viewModelScope.launch {
            repository.isDriveLoading.collect { loading ->
                _uiState.update { it.copy(isDriveLoading = loading) }
            }
        }
        viewModelScope.launch {
            repository.isDrivePaginating.collect { paginating ->
                _uiState.update { it.copy(isDrivePaginating = paginating) }
            }
        }
        viewModelScope.launch {
            repository.driveError.collect { error ->
                _uiState.update { it.copy(driveError = error) }
            }
        }
        viewModelScope.launch {
            repository.hasNextPage.collect { hasNext ->
                _uiState.update { it.copy(hasNextDrivePage = hasNext) }
            }
        }

        // Observe Local Offline Data
        viewModelScope.launch {
            repository.downloadedTracks.collect { tracks ->
                val statusMap = tracks.associate { it.id to TrackDownloadStatus.DOWNLOADED }
                _uiState.update { state ->
                    val updatedCurrent = state.currentTrack?.let { curr ->
                        tracks.find { it.id == curr.id } ?: curr
                    }
                    state.copy(
                        downloadedTracks = tracks,
                        currentTrack = updatedCurrent,
                        downloadStatusByFileId = state.downloadStatusByFileId + statusMap
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.albums.collect { albumList ->
                _uiState.update { it.copy(albums = albumList) }
            }
        }
        viewModelScope.launch {
            repository.playlists.collect { playlistList ->
                _uiState.update { it.copy(playlists = playlistList) }
            }
        }
    }

    fun getGoogleSignInIntent(): Intent = authManager.getSignInIntent()

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            handleSignInResult(data)
        }
    }

    suspend fun handleSignInResult(data: Intent?) {
        val result = authManager.handleSignInResult(data)
        result.onSuccess { user ->
            showToast("Welcome back, ${user.displayName}!")
            loadDriveRoot()
        }.onFailure { e ->
            showToast(e.localizedMessage ?: "Sign in failed. Try again.")
        }
    }

    fun loadDriveRoot(isRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.loadFolder(folderId = "root", folderName = "Your Drive", isRefresh = isRefresh)
        }
    }

    fun openDriveFolder(folderId: String, folderName: String) {
        viewModelScope.launch {
            repository.loadFolder(folderId = folderId, folderName = folderName)
        }
    }

    fun loadNextDrivePage() {
        viewModelScope.launch {
            repository.loadNextPage()
        }
    }

    fun navigateDriveBack(): Boolean {
        val handled = repository.navigateDriveBack()
        if (handled) {
            val last = _uiState.value.driveBreadcrumbs.lastOrNull()
            viewModelScope.launch {
                repository.loadFolder(last?.folderId ?: "root", last?.folderName ?: "Your Drive")
            }
        }
        return handled
    }

    fun refreshDrive() {
        val current = _uiState.value.driveBreadcrumbs.lastOrNull()
        viewModelScope.launch {
            repository.loadFolder(current?.folderId ?: "root", current?.folderName ?: "Your Drive", isRefresh = true)
            showToast("Drive refreshed")
        }
    }

    fun saveDriveFileOffline(item: DriveAudioItem) {
        val currentStatus = _uiState.value.downloadStatusByFileId[item.id]
        if (currentStatus == TrackDownloadStatus.DOWNLOADING) return
        if (currentStatus == TrackDownloadStatus.DOWNLOADED && repository.isFileSavedOffline(item.id)) {
            showToast("\"${item.name}\" is already saved offline")
            return
        }

        _uiState.update { state ->
            state.copy(
                downloadStatusByFileId = state.downloadStatusByFileId + (item.id to TrackDownloadStatus.DOWNLOADING),
                downloadProgressByFileId = state.downloadProgressByFileId + (item.id to 0)
            )
        }

        viewModelScope.launch {
            val result = repository.saveDriveAudioOffline(item) { progress ->
                _uiState.update { state ->
                    state.copy(downloadProgressByFileId = state.downloadProgressByFileId + (item.id to progress))
                }
            }

            result.onSuccess { track ->
                _uiState.update { state ->
                    state.copy(
                        downloadStatusByFileId = state.downloadStatusByFileId + (item.id to TrackDownloadStatus.DOWNLOADED),
                        downloadProgressByFileId = state.downloadProgressByFileId + (item.id to 100)
                    )
                }
                showToast("Saved \"${track.title}\" offline")
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        downloadStatusByFileId = state.downloadStatusByFileId + (item.id to TrackDownloadStatus.FAILED)
                    )
                }
                showToast("Download failed: ${error.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun downloadCurrentFolder() {
        if (_uiState.value.isFolderDownloading) {
            showToast("A folder download is already in progress")
            return
        }

        val currentBreadcrumb = _uiState.value.driveBreadcrumbs.lastOrNull()
        val folderId = currentBreadcrumb?.folderId ?: "root"
        val folderName = currentBreadcrumb?.folderName ?: "Your Drive"

        _uiState.update { state ->
            state.copy(
                isFolderDownloading = true,
                activeFolderDownloadName = folderName,
                folderDownloadCompletedCount = 0,
                folderDownloadTotalCount = 0
            )
        }

        viewModelScope.launch {
            val result = repository.downloadDriveFolder(
                folderId = folderId,
                folderName = folderName,
                onProgress = { completed, total, fileName ->
                    _uiState.update { state ->
                        state.copy(
                            folderDownloadCompletedCount = completed,
                            folderDownloadTotalCount = total,
                            activeFolderDownloadName = "$folderName: $fileName ($completed/$total)"
                        )
                    }
                }
            )

            result.onSuccess { playlist ->
                _uiState.update { state ->
                    state.copy(
                        isFolderDownloading = false,
                        activeFolderDownloadName = null
                    )
                }
                showToast("Downloaded \"${playlist.title}\" (${playlist.trackIds.size} tracks)")
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isFolderDownloading = false,
                        activeFolderDownloadName = null
                    )
                }
                showToast("Folder download failed: ${error.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun toggleDriveFileSelection(fileId: String) {
        _uiState.update { state ->
            val set = state.selectedDriveFileIds.toMutableSet()
            if (set.contains(fileId)) set.remove(fileId) else set.add(fileId)
            state.copy(selectedDriveFileIds = set)
        }
    }

    fun selectAllDriveFiles() {
        val allIds = (_uiState.value.driveAudioFiles + _uiState.value.sharedAudioFiles).map { it.id }.toSet()
        _uiState.update { it.copy(selectedDriveFileIds = allIds) }
    }

    fun clearDriveFileSelection() {
        _uiState.update { it.copy(selectedDriveFileIds = emptySet()) }
    }

    fun downloadSelectedDriveFiles() {
        val selectedIds = _uiState.value.selectedDriveFileIds
        if (selectedIds.isEmpty()) return

        val allAudioItems = _uiState.value.driveAudioFiles + _uiState.value.sharedAudioFiles
        val itemsToDownload = allAudioItems.filter { selectedIds.contains(it.id) }.distinctBy { it.id }
        clearDriveFileSelection()

        showToast("Saving ${itemsToDownload.size} audio files offline...")

        viewModelScope.launch {
            var successCount = 0
            for (item in itemsToDownload) {
                val res = repository.saveDriveAudioOffline(item)
                if (res.isSuccess) successCount++
            }
            showToast("Saved $successCount of ${itemsToDownload.size} files offline")
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = destination) }
    }

    fun openAlbum(albumId: String) {
        _uiState.update { it.copy(selectedAlbumId = albumId, currentScreen = ScreenDestination.ALBUM_DETAIL) }
    }

    fun openPlaylist(playlistId: String) {
        _uiState.update { it.copy(selectedPlaylistId = playlistId, currentScreen = ScreenDestination.PLAYLIST_DETAIL) }
    }

    fun openNowPlaying() {
        _uiState.update { it.copy(isNowPlayingOpen = true) }
    }

    fun closeNowPlaying() {
        _uiState.update { it.copy(isNowPlayingOpen = false) }
    }

    // ==========================================
    // Real Offline Audio Playback Methods
    // ==========================================

    fun playTrack(track: Track) {
        val localPath = track.localFilePath
        if (localPath.isNullOrEmpty()) {
            showToast("Song is not downloaded. Save offline first.")
            return
        }

        val success = audioPlayer.playFile(
            filePath = localPath,
            onCompletion = {
                handleTrackCompletion()
            },
            onError = { errorMsg ->
                _uiState.update { it.copy(isPlaying = false) }
                showToast("Playback error: $errorMsg. Please save offline again.")
            }
        )

        if (success) {
            _uiState.update {
                it.copy(
                    currentTrack = track,
                    isPlaying = true,
                    currentPlaybackSeconds = 0
                )
            }
            startPlaybackProgressTicker()
        } else {
            _uiState.update { it.copy(isPlaying = false) }
            showToast("Cannot play file. Please verify or save offline again.")
        }
    }

    fun playAlbum(album: Album) {
        val tracks = _uiState.value.downloadedTracks.filter { album.trackIds.contains(it.id) }
        if (tracks.isNotEmpty()) {
            playTrack(tracks.first())
        } else {
            showToast("No offline tracks in this album.")
        }
    }

    fun playPlaylist(playlist: Playlist) {
        val tracks = _uiState.value.downloadedTracks.filter { playlist.trackIds.contains(it.id) }
        if (tracks.isNotEmpty()) {
            playTrack(tracks.first())
        } else {
            showToast("No offline tracks in this playlist.")
        }
    }

    private fun handleTrackCompletion() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                _uiState.value.currentTrack?.let { playTrack(it) }
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val tracks = _uiState.value.downloadedTracks
                val currentIdx = tracks.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
                if (currentIdx != -1 && currentIdx < tracks.size - 1) {
                    playNext()
                } else {
                    _uiState.update { it.copy(isPlaying = false, currentPlaybackSeconds = 0) }
                    playbackTimerJob?.cancel()
                }
            }
        }
    }

    private fun startPlaybackProgressTicker() {
        playbackTimerJob?.cancel()
        playbackTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(500)
                if (audioPlayer.isPlaying) {
                    val pos = audioPlayer.currentPositionSeconds
                    _uiState.update {
                        it.copy(
                            currentPlaybackSeconds = pos,
                            isPlaying = true
                        )
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val isCurrentlyPlaying = _uiState.value.isPlaying
        if (isCurrentlyPlaying) {
            audioPlayer.pause()
            _uiState.update { it.copy(isPlaying = false) }
            playbackTimerJob?.cancel()
        } else {
            val currentTrack = _uiState.value.currentTrack
            if (currentTrack != null) {
                val resumed = audioPlayer.resume()
                if (resumed) {
                    _uiState.update { it.copy(isPlaying = true) }
                    startPlaybackProgressTicker()
                } else {
                    playTrack(currentTrack)
                }
            }
        }
    }

    fun playNext() {
        val tracks = _uiState.value.downloadedTracks
        if (tracks.isEmpty()) return

        if (_uiState.value.isShuffleEnabled && tracks.size > 1) {
            val currentId = _uiState.value.currentTrack?.id
            val otherTracks = tracks.filterNot { it.id == currentId }
            val randomTrack = otherTracks.randomOrNull() ?: tracks.first()
            playTrack(randomTrack)
            return
        }

        val currentIdx = tracks.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val nextIdx = if (currentIdx == -1) 0 else (currentIdx + 1) % tracks.size
        playTrack(tracks[nextIdx])
    }

    fun playPrevious() {
        val tracks = _uiState.value.downloadedTracks
        if (tracks.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (_uiState.value.currentPlaybackSeconds > 3) {
            seekTo(0)
            return
        }

        val currentIdx = tracks.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val prevIdx = if (currentIdx <= 0) tracks.size - 1 else currentIdx - 1
        playTrack(tracks[prevIdx])
    }

    fun seekTo(seconds: Int) {
        audioPlayer.seekTo(seconds)
        _uiState.update { it.copy(currentPlaybackSeconds = seconds) }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun toggleRepeat() {
        val modes = RepeatMode.values()
        val next = modes[(_uiState.value.repeatMode.ordinal + 1) % modes.size]
        _uiState.update { it.copy(repeatMode = next) }
    }

    fun createPlaylist(title: String, description: String) {
        repository.createPlaylist(title, description, emptyList())
        showToast("Created playlist \"$title\"")
    }

    fun clearDownloadedStorage() {
        audioPlayer.stopAndRelease()
        playbackTimerJob?.cancel()
        repository.clearAllDownloadedMusic()
        _uiState.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                currentPlaybackSeconds = 0
            )
        }
        showToast("Cleared downloaded files")
    }

    fun signOut() {
        audioPlayer.stopAndRelease()
        playbackTimerJob?.cancel()
        repository.signOut {
            _uiState.update {
                it.copy(
                    currentScreen = ScreenDestination.ONBOARDING,
                    authenticatedUser = null,
                    authState = AuthState.Unauthenticated,
                    driveFolders = emptyList(),
                    driveAudioFiles = emptyList(),
                    sharedFolders = emptyList(),
                    sharedAudioFiles = emptyList(),
                    sharedDrives = emptyList(),
                    driveStorageQuota = null
                )
            }
            showToast("Signed out of Google Drive")
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(activeToast = message) }
        viewModelScope.launch {
            delay(2500)
            if (_uiState.value.activeToast == message) {
                _uiState.update { it.copy(activeToast = null) }
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        repository.addTrackToPlaylist(playlistId, trackId)
        val pl = _uiState.value.playlists.find { it.id == playlistId }
        val track = _uiState.value.downloadedTracks.find { it.id == trackId }
        showToast("Added \"${track?.title ?: "track"}\" to \"${pl?.title ?: "playlist"}\"")
    }

    fun addAlbumToPlaylist(playlistId: String, albumId: String) {
        val album = _uiState.value.albums.find { it.id == albumId } ?: return
        for (trackId in album.trackIds) {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
        val pl = _uiState.value.playlists.find { it.id == playlistId }
        showToast("Added album \"${album.title}\" to \"${pl?.title ?: "playlist"}\"")
    }

    fun removeTrackFromLibrary(trackId: String) {
        val track = _uiState.value.downloadedTracks.find { it.id == trackId }
        if (_uiState.value.currentTrack?.id == trackId) {
            audioPlayer.stopAndRelease()
            _uiState.update { it.copy(isPlaying = false, currentTrack = null, currentPlaybackSeconds = 0) }
            playbackTimerJob?.cancel()
        }
        repository.removeTrack(trackId)
        showToast("Removed \"${track?.title ?: "track"}\" from Library")
    }

    fun deletePlaylist(playlistId: String) {
        val pl = _uiState.value.playlists.find { it.id == playlistId }
        repository.deletePlaylist(playlistId)
        if (_uiState.value.currentScreen == ScreenDestination.PLAYLIST_DETAIL && _uiState.value.selectedPlaylistId == playlistId) {
            _uiState.update { it.copy(currentScreen = ScreenDestination.PLAYLISTS, selectedPlaylistId = null) }
        }
        showToast("Deleted playlist \"${pl?.title ?: ""}\"")
    }

    fun renamePlaylist(playlistId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        repository.renamePlaylist(playlistId, newTitle.trim())
        showToast("Renamed playlist to \"${newTitle.trim()}\"")
    }

    fun uploadPhoneAudioFiles(uris: List<android.net.Uri>, destinationFolderId: String?, destinationFolderName: String) {
        if (uris.isEmpty()) return
        if (_uiState.value.isUploading) {
            showToast("An upload is already in progress.")
            return
        }

        val totalFiles = uris.size
        _uiState.update {
            it.copy(
                isUploading = true,
                uploadProgress = 0,
                uploadFileName = "Preparing audio...",
                uploadStatusMessage = "Processing 1 of $totalFiles"
            )
        }

        viewModelScope.launch {
            var successCount = 0
            var localOnlyCount = 0

            for ((index, uri) in uris.withIndex()) {
                val fileNum = index + 1
                _uiState.update {
                    it.copy(
                        uploadProgress = (index * 100 / totalFiles),
                        uploadFileName = "Uploading song ($fileNum/$totalFiles)...",
                        uploadStatusMessage = "Uploading to Google Drive & Saving locally"
                    )
                }

                val result = repository.uploadPhoneAudio(
                    uri = uri,
                    destinationFolderId = destinationFolderId,
                    destinationFolderName = destinationFolderName,
                    onProgress = { progressInFile ->
                        val overall = ((index * 100) + progressInFile) / totalFiles
                        _uiState.update {
                            it.copy(uploadProgress = overall)
                        }
                    }
                )

                if (result.isSuccess) {
                    successCount++
                } else {
                    val msg = result.exceptionOrNull()?.message ?: ""
                    if (msg.contains("Saved to DriveTune locally")) {
                        localOnlyCount++
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isUploading = false,
                    uploadProgress = 100,
                    uploadFileName = null,
                    uploadStatusMessage = null
                )
            }

            when {
                successCount == totalFiles -> {
                    showToast(if (totalFiles == 1) "Uploaded to Google Drive & Saved to Library" else "Added $totalFiles songs to Library & Google Drive")
                }
                localOnlyCount > 0 && successCount == 0 -> {
                    showToast("Saved to DriveTune locally, but Google Drive upload failed. Check connection.")
                }
                else -> {
                    showToast("Saved $successCount of $totalFiles songs to Google Drive & Library.")
                }
            }
        }
    }

    suspend fun getAvailableDriveFolders(parentFolderId: String? = null): List<com.drivetune.app.drive.DriveFolderItem> {
        return repository.getAvailableDriveFolders(parentFolderId)
    }

    fun createNewDriveFolder(folderName: String, parentFolderId: String?, onCreated: (com.drivetune.app.drive.DriveFolderItem?) -> Unit) {
        if (folderName.isBlank()) return
        viewModelScope.launch {
            val res = repository.createDriveFolder(folderName.trim(), parentFolderId)
            res.onSuccess { folder ->
                showToast("Created folder \"${folder.name}\" in Google Drive")
                onCreated(folder)
            }.onFailure { e ->
                showToast("Failed to create folder: ${e.localizedMessage}")
                onCreated(null)
            }
        }
    }

    fun dismissUploadProgress() {
        _uiState.update {
            it.copy(
                isUploading = false,
                uploadProgress = 0,
                uploadFileName = null,
                uploadStatusMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAndRelease()
        playbackTimerJob?.cancel()
    }
}
