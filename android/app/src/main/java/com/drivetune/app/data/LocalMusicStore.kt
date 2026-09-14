package com.drivetune.app.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.drivetune.app.model.Album
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class AudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val bitrate: String
)

class LocalMusicStore(private val context: Context) {

    private val gson = Gson()
    val audioDir: File by lazy {
        File(context.filesDir, "drivetune_audio").apply {
            if (!exists()) mkdirs()
        }
    }

    private val libraryFile: File by lazy {
        File(context.filesDir, "drivetune_library.json")
    }

    private data class LibraryData(
        val tracks: List<Track> = emptyList(),
        val playlists: List<Playlist> = emptyList()
    )

    @Synchronized
    fun getDownloadedTracks(): List<Track> {
        val data = loadLibraryData()
        // Verify that local file physically exists and has valid bytes on disk
        return data.tracks.filter { track ->
            val path = track.localFilePath
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                file.exists() && file.canRead() && file.length() > 0
            } else {
                false
            }
        }.map { it.copy(status = TrackDownloadStatus.DOWNLOADED, downloadProgress = 100) }
    }

    @Synchronized
    fun getAlbums(): List<Album> {
        val tracks = getDownloadedTracks()
        if (tracks.isEmpty()) return emptyList()

        return tracks.groupBy { it.album.ifEmpty { "Drive Music" } }
            .map { (albumTitle, albumTracks) ->
                val first = albumTracks.first()
                val artists = albumTracks.map { it.artist }.filter { it.isNotBlank() && it != "Google Drive" }.distinct()
                val artistName = when {
                    artists.size == 1 -> artists.first()
                    artists.size > 1 -> "Various Artists"
                    else -> "Google Drive"
                }
                Album(
                    id = "alb-${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = artistName,
                    year = "",
                    folderId = first.folderId,
                    folderPath = albumTitle,
                    coverArtUrl = "",
                    trackIds = albumTracks.map { it.id }
                )
            }
    }

    @Synchronized
    fun saveTrack(track: Track) {
        val current = loadLibraryData()
        val updatedTracks = current.tracks.filterNot { it.id == track.id } + track
        saveLibraryData(current.copy(tracks = updatedTracks))
    }

    @Synchronized
    fun removeTrack(trackId: String) {
        val current = loadLibraryData()
        val track = current.tracks.find { it.id == trackId }
        if (track?.localFilePath != null) {
            val file = File(track.localFilePath)
            if (file.exists()) file.delete()
        }
        val updatedTracks = current.tracks.filterNot { it.id == trackId }
        saveLibraryData(current.copy(tracks = updatedTracks))
    }

    @Synchronized
    fun getPlaylists(): List<Playlist> {
        val data = loadLibraryData()
        val validTrackIds = getDownloadedTracks().map { it.id }.toSet()
        return data.playlists.map { playlist ->
            playlist.copy(trackIds = playlist.trackIds.filter { validTrackIds.contains(it) })
        }.filter { it.trackIds.isNotEmpty() }
    }

    @Synchronized
    fun savePlaylist(playlist: Playlist) {
        val current = loadLibraryData()
        val existing = current.playlists.find { it.id == playlist.id || it.title.equals(playlist.title, ignoreCase = true) }
        val mergedTrackIds = if (existing != null) {
            (existing.trackIds + playlist.trackIds).distinct()
        } else {
            playlist.trackIds.distinct()
        }
        val targetId = existing?.id ?: playlist.id
        val updatedPlaylist = playlist.copy(id = targetId, trackIds = mergedTrackIds)
        val updatedPlaylists = listOf(updatedPlaylist) + current.playlists.filterNot { it.id == targetId || it.title.equals(playlist.title, ignoreCase = true) }
        saveLibraryData(current.copy(playlists = updatedPlaylists))
    }

    @Synchronized
    fun deletePlaylist(playlistId: String) {
        val current = loadLibraryData()
        val updatedPlaylists = current.playlists.filterNot { it.id == playlistId }
        saveLibraryData(current.copy(playlists = updatedPlaylists))
    }

    @Synchronized
    fun renamePlaylist(playlistId: String, newTitle: String) {
        val current = loadLibraryData()
        val updatedPlaylists = current.playlists.map {
            if (it.id == playlistId) it.copy(title = newTitle) else it
        }
        saveLibraryData(current.copy(playlists = updatedPlaylists))
    }

    @Synchronized
    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val current = loadLibraryData()
        val pl = current.playlists.find { it.id == playlistId }
        if (pl != null && !pl.trackIds.contains(trackId)) {
            val updated = pl.copy(trackIds = pl.trackIds + trackId)
            savePlaylist(updated)
        }
    }

    @Synchronized
    fun isTrackDownloaded(driveFileId: String): Boolean {
        val tracks = getDownloadedTracks()
        return tracks.any { it.id == driveFileId && it.status == TrackDownloadStatus.DOWNLOADED }
    }

    @Synchronized
    fun getTrackLocalFile(driveFileId: String, extension: String): File {
        val sanitizedExtension = if (extension.startsWith(".")) extension else ".$extension"
        return File(audioDir, "${driveFileId}$sanitizedExtension")
    }

    @Synchronized
    fun extractAudioMetadata(
        file: File,
        fallbackTitle: String,
        folderName: String
    ): AudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val tagTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val tagArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val tagAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val tagDurationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val tagBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)

            val durationMs = tagDurationStr?.toLongOrNull() ?: 0L
            val durationSec = (durationMs / 1000L).toInt()
            val bitrateFormatted = if (!tagBitrate.isNullOrBlank()) {
                val kbps = tagBitrate.toIntOrNull()?.let { it / 1000 } ?: 0
                if (kbps > 0) "$kbps kbps" else ""
            } else ""

            // Folder is the primary album organizer when downloading a folder
            val finalAlbum = if (folderName.isNotBlank() && folderName != "Your Drive" && folderName != "root" && folderName != "Drive Downloads" && folderName != "Drive Music") {
                folderName
            } else if (!tagAlbum.isNullOrBlank()) {
                tagAlbum
            } else {
                "Drive Music"
            }

            AudioMetadata(
                title = if (!tagTitle.isNullOrBlank()) tagTitle else fallbackTitle,
                artist = if (!tagArtist.isNullOrBlank()) tagArtist else "Google Drive",
                album = finalAlbum,
                durationSeconds = durationSec,
                bitrate = bitrateFormatted
            )
        } catch (e: Exception) {
            val finalAlbum = if (folderName.isNotBlank() && folderName != "Your Drive" && folderName != "root" && folderName != "Drive Downloads") folderName else "Drive Music"
            AudioMetadata(
                title = fallbackTitle,
                artist = "Google Drive",
                album = finalAlbum,
                durationSeconds = 0,
                bitrate = ""
            )
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    @Synchronized
    fun getOfflineStorageBytes(): Long {
        return audioDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    @Synchronized
    fun clearAllDownloadedMusic() {
        audioDir.listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
        val current = loadLibraryData()
        saveLibraryData(current.copy(tracks = emptyList(), playlists = emptyList()))
    }

    private fun loadLibraryData(): LibraryData {
        return try {
            if (libraryFile.exists()) {
                val json = libraryFile.readText()
                val type = object : TypeToken<LibraryData>() {}.type
                gson.fromJson(json, type) ?: LibraryData()
            } else {
                LibraryData()
            }
        } catch (e: Exception) {
            LibraryData()
        }
    }

    private fun saveLibraryData(data: LibraryData) {
        try {
            val json = gson.toJson(data)
            libraryFile.writeText(json)
        } catch (e: Exception) {
            // Log/ignore safely
        }
    }
}
