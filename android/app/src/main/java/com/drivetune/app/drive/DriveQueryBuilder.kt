package com.drivetune.app.drive

object DriveQueryBuilder {

    const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"

    val AUDIO_MIME_TYPES = setOf(
        "audio/mpeg",
        "audio/mp3",
        "audio/mp4",
        "audio/x-m4a",
        "audio/m4a",
        "audio/wav",
        "audio/x-wav",
        "audio/flac",
        "audio/x-flac",
        "audio/ogg",
        "audio/x-ogg",
        "audio/aac",
        "audio/x-aac",
        "audio/webm",
        "audio/opus",
        "audio/x-ms-wma",
        "audio/x-aiff",
        "application/ogg",
        "application/x-flac"
    )

    val AUDIO_EXTENSIONS = setOf(
        "mp3", "flac", "m4a", "wav", "ogg", "aac", "webm", "opus", "wma", "aiff", "mp4"
    )

    const val AUDIO_FILE_FILTER = "(" +
        "mimeType contains 'audio/' or " +
        "mimeType = 'application/ogg' or " +
        "mimeType = 'application/x-flac' or " +
        "fileExtension = 'mp3' or " +
        "fileExtension = 'flac' or " +
        "fileExtension = 'm4a' or " +
        "fileExtension = 'wav' or " +
        "fileExtension = 'ogg' or " +
        "fileExtension = 'aac' or " +
        "fileExtension = 'webm' or " +
        "fileExtension = 'opus' or " +
        "fileExtension = 'wma' or " +
        "fileExtension = 'aiff')"

    const val AUDIO_AND_FOLDER_FILTER = "(" +
        "mimeType = '$FOLDER_MIME_TYPE' or " +
        "mimeType contains 'audio/' or " +
        "mimeType = 'application/ogg' or " +
        "mimeType = 'application/x-flac' or " +
        "fileExtension = 'mp3' or " +
        "fileExtension = 'flac' or " +
        "fileExtension = 'm4a' or " +
        "fileExtension = 'wav' or " +
        "fileExtension = 'ogg' or " +
        "fileExtension = 'aac' or " +
        "fileExtension = 'webm' or " +
        "fileExtension = 'opus' or " +
        "fileExtension = 'wma' or " +
        "fileExtension = 'aiff')"

    fun buildQueryForFolder(folderId: String?): String {
        val parentTarget = if (folderId.isNullOrEmpty() || folderId == "root") "root" else folderId
        return "trashed = false and '$parentTarget' in parents and $AUDIO_AND_FOLDER_FILTER"
    }

    fun buildSharedWithMeQuery(): String {
        return "trashed = false and sharedWithMe = true and $AUDIO_AND_FOLDER_FILTER"
    }

    fun buildAllAudioQuery(): String {
        return "trashed = false and $AUDIO_FILE_FILTER"
    }

    fun buildAllAudioInFolderQuery(folderId: String): String {
        return "trashed = false and '$folderId' in parents and $AUDIO_FILE_FILTER"
    }

    fun isAudioFile(mimeType: String?, fileName: String?): Boolean {
        if (mimeType == FOLDER_MIME_TYPE) return false

        if (mimeType != null && (mimeType.startsWith("audio/") || AUDIO_MIME_TYPES.contains(mimeType.lowercase()))) {
            return true
        }

        val ext = fileName?.substringAfterLast('.', "")?.lowercase()
        return !ext.isNullOrEmpty() && AUDIO_EXTENSIONS.contains(ext)
    }

    fun getFormatExtension(fileName: String?, mimeType: String?): String {
        val ext = fileName?.substringAfterLast('.', "")?.uppercase()
        if (!ext.isNullOrEmpty() && ext.length in 2..5) {
            return ext
        }
        return when (mimeType?.lowercase()) {
            "audio/mpeg", "audio/mp3" -> "MP3"
            "audio/flac", "audio/x-flac", "application/x-flac" -> "FLAC"
            "audio/wav", "audio/x-wav" -> "WAV"
            "audio/x-m4a", "audio/m4a", "audio/mp4" -> "M4A"
            "audio/ogg", "audio/x-ogg", "application/ogg" -> "OGG"
            "audio/aac", "audio/x-aac" -> "AAC"
            "audio/webm" -> "WEBM"
            "audio/opus" -> "OPUS"
            else -> "AUDIO"
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "%.1f GB".format(gb)
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.0f KB".format(kb)
            else -> "$bytes B"
        }
    }
}

