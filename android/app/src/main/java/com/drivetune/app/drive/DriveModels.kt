package com.drivetune.app.drive

enum class DriveSourceType {
    MY_DRIVE,
    SHARED_WITH_ME,
    SHARED_DRIVE
}

data class DriveAudioItem(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val formatExtension: String,
    val modifiedTime: String,
    val parentFolderId: String?,
    val webViewLink: String?,
    val iconLink: String?,
    val driveId: String? = null,
    val sourceType: DriveSourceType = DriveSourceType.MY_DRIVE,
    val isShared: Boolean = false
)

data class DriveFolderItem(
    val id: String,
    val name: String,
    val parentId: String?,
    val modifiedTime: String,
    val driveId: String? = null,
    val sourceType: DriveSourceType = DriveSourceType.MY_DRIVE,
    val isShared: Boolean = false
)

data class DriveBreadcrumb(
    val folderId: String,
    val folderName: String,
    val driveId: String? = null,
    val sourceType: DriveSourceType = DriveSourceType.MY_DRIVE
)

data class DriveStorageQuota(
    val usageBytes: Long,
    val limitBytes: Long,
    val usageFormatted: String,
    val limitFormatted: String,
    val usagePercent: Float
)

data class DrivePageResult(
    val folders: List<DriveFolderItem>,
    val audioFiles: List<DriveAudioItem>,
    val sharedFolders: List<DriveFolderItem> = emptyList(),
    val sharedAudioFiles: List<DriveAudioItem> = emptyList(),
    val sharedDrives: List<DriveFolderItem> = emptyList(),
    val nextPageToken: String?,
    val currentFolderId: String?,
    val currentFolderName: String,
    val isRoot: Boolean = false
)

sealed interface DriveResult<out T> {
    data class Success<T>(val data: T) : DriveResult<T>
    data class Error(val message: String, val isAuthExpired: Boolean = false, val isOffline: Boolean = false) : DriveResult<Nothing>
    data object Loading : DriveResult<Nothing>
}

