package com.drivetune.app.data

import com.drivetune.app.auth.AuthManager
import com.drivetune.app.auth.AuthState
import com.drivetune.app.auth.AuthenticatedUser
import com.drivetune.app.drive.DriveAudioItem
import com.drivetune.app.drive.DriveBreadcrumb
import com.drivetune.app.drive.DriveFolderItem
import com.drivetune.app.drive.DrivePageResult
import com.drivetune.app.drive.DriveResult
import com.drivetune.app.drive.DriveService
import com.drivetune.app.drive.DriveStorageQuota
import com.drivetune.app.drive.GoogleDriveServiceImpl
import com.drivetune.app.model.Album
import com.drivetune.app.model.DriveFolder
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import android.content.Context
import java.io.File

interface DriveTuneRepository {
    val authState: StateFlow<AuthState>
    val realDriveFolders: Flow<List<DriveFolderItem>>
    val realDriveAudioFiles: Flow<List<DriveAudioItem>>
    val driveStorageQuota: Flow<DriveStorageQuota?>
    val breadcrumbStack: Flow<List<DriveBreadcrumb>>
    val isDriveLoading: Flow<Boolean>
    val isDrivePaginating: Flow<Boolean>
    val driveError: Flow<String?>
    val hasNextPage: Flow<Boolean>

    // Local library & offline domain flows
    val downloadedTracks: Flow<List<Track>>
    val albums: Flow<List<Album>>
    val playlists: Flow<List<Playlist>>

    suspend fun loadFolder(folderId: String? = null, folderName: String = "Your Drive", isRefresh: Boolean = false)
    suspend fun loadNextPage()
    fun navigateDriveBack(): Boolean
    fun getTrack(id: String): Track?
    fun getAlbum(id: String): Album?
    fun getPlaylist(id: String): Playlist?
    fun updateTrackDownloadStatus(trackId: String, status: TrackDownloadStatus, progress: Int)
    fun createPlaylist(title: String, description: String, trackIds: List<String>)
    fun removeTrack(trackId: String)
    fun deletePlaylist(playlistId: String)
    fun renamePlaylist(playlistId: String, newTitle: String)
    fun addTrackToPlaylist(playlistId: String, trackId: String)
    suspend fun createDriveFolder(folderName: String, parentFolderId: String? = null): Result<DriveFolderItem>
    suspend fun saveDriveAudioOffline(audioItem: DriveAudioItem, overrideFolderName: String? = null, onProgress: (Int) -> Unit = {}): Result<Track>
    suspend fun downloadDriveFolder(folderId: String, folderName: String, onProgress: (completed: Int, total: Int, currentFileName: String) -> Unit): Result<Playlist>
    suspend fun uploadPhoneAudio(uri: android.net.Uri, destinationFolderId: String? = null, destinationFolderName: String = "My Drive", onProgress: (Int) -> Unit = {}): Result<Track>
    suspend fun getAvailableDriveFolders(parentFolderId: String? = null): List<DriveFolderItem>
    fun checkForDuplicate(fileName: String): Track?
    fun isFileSavedOffline(fileId: String): Boolean
    fun clearAllDownloadedMusic()
    fun signOut(onComplete: () -> Unit)
}

class AppDriveTuneRepository(
    private val authManager: AuthManager,
    private val context: Context,
    private val driveService: DriveService = GoogleDriveServiceImpl(authManager),
    private val localMusicStore: LocalMusicStore = LocalMusicStore(context)
) : DriveTuneRepository {

    override val authState: StateFlow<AuthState> = authManager.authState

    private val _realDriveFolders = MutableStateFlow<List<DriveFolderItem>>(emptyList())
    override val realDriveFolders: Flow<List<DriveFolderItem>> = _realDriveFolders.asStateFlow()

    private val _realDriveAudioFiles = MutableStateFlow<List<DriveAudioItem>>(emptyList())
    override val realDriveAudioFiles: Flow<List<DriveAudioItem>> = _realDriveAudioFiles.asStateFlow()

    private val _driveStorageQuota = MutableStateFlow<DriveStorageQuota?>(null)
    override val driveStorageQuota: Flow<DriveStorageQuota?> = _driveStorageQuota.asStateFlow()

    private val _breadcrumbStack = MutableStateFlow<List<DriveBreadcrumb>>(listOf(DriveBreadcrumb("root", "Your Drive")))
    override val breadcrumbStack: Flow<List<DriveBreadcrumb>> = _breadcrumbStack.asStateFlow()

    private val _isDriveLoading = MutableStateFlow(false)
    override val isDriveLoading: Flow<Boolean> = _isDriveLoading.asStateFlow()

    private val _isDrivePaginating = MutableStateFlow(false)
    override val isDrivePaginating: Flow<Boolean> = _isDrivePaginating.asStateFlow()

    private val _driveError = MutableStateFlow<String?>(null)
    override val driveError: Flow<String?> = _driveError.asStateFlow()

    private val _nextPageToken = MutableStateFlow<String?>(null)
    private val _hasNextPage = MutableStateFlow(false)
    override val hasNextPage: Flow<Boolean> = _hasNextPage.asStateFlow()

    private var currentFolderId: String? = "root"

    // Local offline tracks and albums
    private val _downloadedTracks = MutableStateFlow<List<Track>>(emptyList())
    override val downloadedTracks: Flow<List<Track>> = _downloadedTracks.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: Flow<List<Album>> = _albums.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    override val playlists: Flow<List<Playlist>> = _playlists.asStateFlow()

    override suspend fun loadFolder(folderId: String?, folderName: String, isRefresh: Boolean) {
        val targetId = if (folderId == "root" || folderId.isNullOrEmpty()) null else folderId
        currentFolderId = targetId ?: "root"

        _isDriveLoading.value = true
        _driveError.value = null

        // Update breadcrumb stack
        val currentStack = _breadcrumbStack.value.toMutableList()
        if (targetId == null || targetId == "root") {
            _breadcrumbStack.value = listOf(DriveBreadcrumb("root", "Your Drive"))
        } else {
            val existingIdx = currentStack.indexOfFirst { it.folderId == targetId }
            if (existingIdx != -1) {
                _breadcrumbStack.value = currentStack.subList(0, existingIdx + 1)
            } else {
                currentStack.add(DriveBreadcrumb(targetId, folderName))
                _breadcrumbStack.value = currentStack
            }
        }

        // Fetch Storage Quota in parallel/background
        if (targetId == null || targetId == "root" || _driveStorageQuota.value == null) {
            when (val quotaRes = driveService.getStorageQuota()) {
                is DriveResult.Success -> _driveStorageQuota.value = quotaRes.data
                else -> {}
            }
        }

        when (val result = driveService.getFolderContents(targetId, folderName)) {
            is DriveResult.Success -> {
                _realDriveFolders.value = result.data.folders
                _realDriveAudioFiles.value = result.data.audioFiles
                _nextPageToken.value = result.data.nextPageToken
                _hasNextPage.value = !result.data.nextPageToken.isNullOrEmpty()
                _isDriveLoading.value = false
            }
            is DriveResult.Error -> {
                _isDriveLoading.value = false
                _driveError.value = result.message
            }
            DriveResult.Loading -> {
                _isDriveLoading.value = true
            }
        }
    }

    override suspend fun loadNextPage() {
        val token = _nextPageToken.value
        if (token.isNullOrEmpty() || _isDrivePaginating.value || _isDriveLoading.value) return

        _isDrivePaginating.value = true
        val targetId = if (currentFolderId == "root") null else currentFolderId
        val currentName = _breadcrumbStack.value.lastOrNull()?.folderName ?: "Drive"

        when (val result = driveService.getFolderContents(targetId, currentName, pageToken = token)) {
            is DriveResult.Success -> {
                _realDriveFolders.update { it + result.data.folders }
                _realDriveAudioFiles.update { it + result.data.audioFiles }
                _nextPageToken.value = result.data.nextPageToken
                _hasNextPage.value = !result.data.nextPageToken.isNullOrEmpty()
                _isDrivePaginating.value = false
            }
            is DriveResult.Error -> {
                _isDrivePaginating.value = false
                _driveError.value = result.message
            }
            DriveResult.Loading -> {}
        }
    }

    init {
        // Load offline library from local persistent store
        refreshLocalLibrary()
    }

    private fun refreshLocalLibrary() {
        _downloadedTracks.value = localMusicStore.getDownloadedTracks()
        _albums.value = localMusicStore.getAlbums()
        _playlists.value = localMusicStore.getPlaylists()
    }

    override fun isFileSavedOffline(fileId: String): Boolean {
        return localMusicStore.isTrackDownloaded(fileId)
    }

    override suspend fun saveDriveAudioOffline(
        audioItem: DriveAudioItem,
        overrideFolderName: String?,
        onProgress: (Int) -> Unit
    ): Result<Track> {
        val extension = if (audioItem.formatExtension.isNotBlank()) audioItem.formatExtension else "mp3"
        val destinationFile = localMusicStore.getTrackLocalFile(audioItem.id, extension)

        // If file already exists and valid, skip download
        if (destinationFile.exists() && destinationFile.length() > 0 && localMusicStore.isTrackDownloaded(audioItem.id)) {
            val existing = _downloadedTracks.value.find { it.id == audioItem.id }
            if (existing != null) {
                onProgress(100)
                return Result.success(existing)
            }
        }

        when (val downloadRes = driveService.downloadFile(audioItem.id, destinationFile, audioItem.sizeBytes, onProgress)) {
            is DriveResult.Success -> {
                val currentFolder = overrideFolderName ?: _breadcrumbStack.value.lastOrNull()?.folderName ?: "Drive Downloads"
                val metadata = localMusicStore.extractAudioMetadata(
                    file = destinationFile,
                    fallbackTitle = audioItem.name.substringBeforeLast("."),
                    folderName = if (currentFolder != "Your Drive" && currentFolder != "root") currentFolder else "Drive Music"
                )

                val track = Track(
                    id = audioItem.id,
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    albumId = "alb-${metadata.album.hashCode()}",
                    folderId = audioItem.parentFolderId ?: "root",
                    durationSeconds = metadata.durationSeconds,
                    format = audioItem.formatExtension.uppercase().removePrefix("."),
                    sizeString = audioItem.sizeFormatted,
                    bitrate = metadata.bitrate,
                    status = TrackDownloadStatus.DOWNLOADED,
                    downloadProgress = 100,
                    coverArtUrl = "",
                    localFilePath = destinationFile.absolutePath
                )
                localMusicStore.saveTrack(track)
                refreshLocalLibrary()
                return Result.success(track)
            }
            is DriveResult.Error -> {
                return Result.failure(Exception(downloadRes.message))
            }
            DriveResult.Loading -> {
                return Result.failure(Exception("Download in progress"))
            }
        }
    }

    override suspend fun downloadDriveFolder(
        folderId: String,
        folderName: String,
        onProgress: (completed: Int, total: Int, currentFileName: String) -> Unit
    ): Result<Playlist> {
        val targetId = if (folderId == "root") null else folderId
        val audiosResult = driveService.getAllAudioFilesInFolder(targetId)

        if (audiosResult is DriveResult.Error) {
            return Result.failure(Exception(audiosResult.message))
        }

        val audios = (audiosResult as? DriveResult.Success)?.data ?: emptyList()
        if (audios.isEmpty()) {
            return Result.failure(Exception("No supported audio files found in \"$folderName\"."))
        }

        val downloadedTrackIds = mutableListOf<String>()
        var completedCount = 0

        for ((index, audio) in audios.withIndex()) {
            onProgress(completedCount, audios.size, audio.name)
            val saveResult = saveDriveAudioOffline(audio, overrideFolderName = folderName)
            if (saveResult.isSuccess) {
                downloadedTrackIds.add(audio.id)
            }
            completedCount = index + 1
            onProgress(completedCount, audios.size, audio.name)
        }

        if (downloadedTrackIds.isEmpty()) {
            return Result.failure(Exception("Failed to download audio files in \"$folderName\"."))
        }

        val playlist = Playlist(
            id = "pl-${folderId.ifEmpty { "root" }}",
            title = folderName,
            description = "Downloaded album/folder from Google Drive ($folderName)",
            trackIds = downloadedTrackIds,
            coverArtUrls = emptyList()
        )

        localMusicStore.savePlaylist(playlist)
        refreshLocalLibrary()

        return Result.success(playlist)
    }

    override fun navigateDriveBack(): Boolean {
        val stack = _breadcrumbStack.value
        if (stack.size <= 1) return false // Already at root

        val newStack = stack.dropLast(1)
        val parent = newStack.last()
        _breadcrumbStack.value = newStack
        currentFolderId = parent.folderId
        return true
    }

    override fun getTrack(id: String): Track? = _downloadedTracks.value.find { it.id == id }

    override fun getAlbum(id: String): Album? = _albums.value.find { it.id == id }

    override fun getPlaylist(id: String): Playlist? = _playlists.value.find { it.id == id }

    override fun updateTrackDownloadStatus(trackId: String, status: TrackDownloadStatus, progress: Int) {
        _downloadedTracks.update { list ->
            list.map { if (it.id == trackId) it.copy(status = status, downloadProgress = progress) else it }
        }
    }

    override fun createPlaylist(title: String, description: String, trackIds: List<String>) {
        val pl = Playlist(
            id = "pl-${System.currentTimeMillis()}",
            title = title,
            description = description,
            trackIds = trackIds,
            coverArtUrls = emptyList()
        )
        localMusicStore.savePlaylist(pl)
        refreshLocalLibrary()
    }

    override fun removeTrack(trackId: String) {
        localMusicStore.removeTrack(trackId)
        refreshLocalLibrary()
    }

    override fun deletePlaylist(playlistId: String) {
        localMusicStore.deletePlaylist(playlistId)
        refreshLocalLibrary()
    }

    override fun renamePlaylist(playlistId: String, newTitle: String) {
        localMusicStore.renamePlaylist(playlistId, newTitle)
        refreshLocalLibrary()
    }

    override fun addTrackToPlaylist(playlistId: String, trackId: String) {
        localMusicStore.addTrackToPlaylist(playlistId, trackId)
        refreshLocalLibrary()
    }

    override suspend fun createDriveFolder(folderName: String, parentFolderId: String?): Result<DriveFolderItem> {
        return when (val res = driveService.createFolder(folderName, parentFolderId)) {
            is DriveResult.Success -> Result.success(res.data)
            is DriveResult.Error -> Result.failure(Exception(res.message))
            DriveResult.Loading -> Result.failure(Exception("Operation in progress"))
        }
    }

    override fun clearAllDownloadedMusic() {
        localMusicStore.clearAllDownloadedMusic()
        refreshLocalLibrary()
    }

    override suspend fun getAvailableDriveFolders(parentFolderId: String?): List<DriveFolderItem> {
        val targetId = if (parentFolderId == "root") null else parentFolderId
        val res = driveService.getFolderContents(targetId, "Drive")
        return if (res is DriveResult.Success) {
            res.data.folders
        } else {
            emptyList()
        }
    }

    override fun checkForDuplicate(fileName: String): Track? {
        val nameWithoutExt = fileName.substringBeforeLast(".")
        return _downloadedTracks.value.find { track ->
            track.title.equals(nameWithoutExt, ignoreCase = true) ||
            track.localFilePath?.let { File(it).name.equals(fileName, ignoreCase = true) } == true
        }
    }

    override suspend fun uploadPhoneAudio(
        uri: android.net.Uri,
        destinationFolderId: String?,
        destinationFolderName: String,
        onProgress: (Int) -> Unit
    ): Result<Track> {
        var originalFileName = "Audio_${System.currentTimeMillis()}.mp3"
        var fileSize = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) cursor.getString(nameIndex)?.let { originalFileName = it }
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }
        } catch (ignored: Exception) {
            uri.lastPathSegment?.let { originalFileName = it }
        }

        val extension = originalFileName.substringAfterLast(".", "mp3").lowercase()
        val tempImportFile = File(context.cacheDir, "import_${System.currentTimeMillis()}_$originalFileName")

        try {
            onProgress(5)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempImportFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Cannot read selected audio file from device storage."))
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to read local audio file: ${e.localizedMessage}"))
        }

        val finalFolderName = if (destinationFolderName.isBlank() || destinationFolderName == "My Drive" || destinationFolderName == "root") "Imported Music" else destinationFolderName.trim()

        val extractedMeta = localMusicStore.extractAudioMetadata(
            file = tempImportFile,
            fallbackTitle = originalFileName.substringBeforeLast("."),
            folderName = finalFolderName
        )

        // 1. Resolve Target Drive Destination Folder ID
        val targetFolderId: String? = if (!destinationFolderId.isNullOrEmpty() && destinationFolderId != "root") {
            destinationFolderId
        } else {
            null // Root of Drive
        }

        val mimeType = context.contentResolver.getType(uri) ?: when (extension) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            else -> "audio/mpeg"
        }

        // 2. Upload to Google Drive
        var driveUploadSuccess = false
        var uploadedDriveItem: DriveAudioItem? = null
        var uploadErrorMessage: String? = null

        when (val uploadRes = driveService.uploadAudioFile(
            file = tempImportFile,
            fileName = originalFileName,
            mimeType = mimeType,
            parentFolderId = targetFolderId,
            onProgress = { p -> onProgress(10 + (p * 80 / 100)) }
        )) {
            is DriveResult.Success -> {
                driveUploadSuccess = true
                uploadedDriveItem = uploadRes.data
            }
            is DriveResult.Error -> {
                uploadErrorMessage = uploadRes.message
            }
            DriveResult.Loading -> {}
        }

        // 3. Save to Local Private Storage
        val assignedId = uploadedDriveItem?.id ?: "loc-${System.currentTimeMillis()}"
        val persistentLocalFile = localMusicStore.getTrackLocalFile(assignedId, extension)
        
        try {
            tempImportFile.copyTo(persistentLocalFile, overwrite = true)
            tempImportFile.delete()
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to save song locally: ${e.localizedMessage}"))
        }

        val track = Track(
            id = assignedId,
            title = extractedMeta.title,
            artist = extractedMeta.artist,
            album = extractedMeta.album,
            albumId = "alb-${extractedMeta.album.hashCode()}",
            folderId = targetFolderId ?: "root",
            durationSeconds = extractedMeta.durationSeconds,
            format = extension.uppercase(),
            sizeString = if (fileSize > 0) com.drivetune.app.drive.DriveQueryBuilder.formatBytes(fileSize) else com.drivetune.app.drive.DriveQueryBuilder.formatBytes(persistentLocalFile.length()),
            bitrate = extractedMeta.bitrate,
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "",
            localFilePath = persistentLocalFile.absolutePath
        )

        localMusicStore.saveTrack(track)
        refreshLocalLibrary()
        onProgress(100)

        return if (driveUploadSuccess) {
            Result.success(track)
        } else {
            // Local save succeeded, upload to Drive failed - return as failure with clear details per Requirement 9
            Result.failure(Exception("Saved to DriveTune locally, but upload to Google Drive failed (${uploadErrorMessage ?: "Check connection"})."))
        }
    }

    override fun signOut(onComplete: () -> Unit) {
        authManager.signOut {
            _realDriveFolders.value = emptyList()
            _realDriveAudioFiles.value = emptyList()
            _driveStorageQuota.value = null
            _breadcrumbStack.value = listOf(DriveBreadcrumb("root", "Your Drive"))
            _driveError.value = null
            onComplete()
        }
    }
}

