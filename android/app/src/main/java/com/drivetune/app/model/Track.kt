package com.drivetune.app.model

enum class TrackDownloadStatus {
    CLOUD,
    DOWNLOADING,
    DOWNLOADED,
    FAILED
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: String,
    val folderId: String,
    val durationSeconds: Int,
    val format: String, // "FLAC", "MP3", "WAV", "M4A"
    val sizeString: String, // "42.4 MB"
    val bitrate: String, // "1411 kbps"
    val status: TrackDownloadStatus = TrackDownloadStatus.CLOUD,
    val downloadProgress: Int = 0, // 0 - 100
    val coverArtUrl: String = "",
    val localFilePath: String? = null
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
