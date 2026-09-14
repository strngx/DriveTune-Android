package com.drivetune.app.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
    val folderId: String,
    val folderPath: String,
    val coverArtUrl: String,
    val trackIds: List<String>
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val trackIds: List<String>,
    val coverArtUrls: List<String>
)

data class DriveFolder(
    val id: String,
    val name: String,
    val path: String,
    val trackCount: Int,
    val sizeString: String
)

data class DownloadItem(
    val trackId: String,
    val trackTitle: String,
    val sizeString: String,
    val progress: Int, // 0 - 100
    val speed: String = "1.8 MB/s",
    val isPaused: Boolean = false
)

data class StorageStats(
    val driveUsed: String = "4.2 GB",
    val driveTotal: String = "15 GB",
    val localOfflineUsed: String = "1.4 GB",
    val totalAudioCount: Int = 18,
    val downloadedCount: Int = 12
)
