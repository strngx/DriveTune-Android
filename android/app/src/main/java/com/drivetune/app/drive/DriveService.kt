package com.drivetune.app.drive

import com.drivetune.app.auth.AuthManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface DriveService {
    suspend fun getFolderContents(
        folderId: String? = null,
        folderName: String = "Your Drive",
        pageToken: String? = null,
        pageSize: Int = 50
    ): DriveResult<DrivePageResult>

    suspend fun getStorageQuota(): DriveResult<DriveStorageQuota>

    suspend fun downloadFile(
        fileId: String,
        destinationFile: File,
        totalSizeBytes: Long = 0L,
        onProgress: (Int) -> Unit = {}
    ): DriveResult<File>

    suspend fun getAllAudioFilesInFolder(
        folderId: String?
    ): DriveResult<List<DriveAudioItem>>

    suspend fun createFolder(
        folderName: String,
        parentFolderId: String? = null
    ): DriveResult<DriveFolderItem>

    suspend fun uploadAudioFile(
        file: File,
        fileName: String,
        mimeType: String,
        parentFolderId: String?,
        onProgress: (Int) -> Unit = {}
    ): DriveResult<DriveAudioItem>

    suspend fun findOrCreateDefaultMusicFolder(): DriveResult<DriveFolderItem>
}

class GoogleDriveServiceImpl(
    private val authManager: AuthManager
) : DriveService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()

    private fun getDriveClient(): Drive? {
        val credential = authManager.getGoogleCredential() ?: return null
        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("DriveTune")
            .build()
    }

    override suspend fun getFolderContents(
        folderId: String?,
        folderName: String,
        pageToken: String?,
        pageSize: Int
    ): DriveResult<DrivePageResult> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error(
            message = "Authentication required. Please connect your Google account.",
            isAuthExpired = true
        )

        try {
            val isRoot = folderId.isNullOrEmpty() || folderId == "root"

            if (isRoot && pageToken.isNullOrEmpty()) {
                // -------------------------------------------------------------
                // 1. ROOT LEVEL: Fetch My Drive, Shared with me, & Shared Drives
                // -------------------------------------------------------------
                val myDriveFolders = mutableListOf<DriveFolderItem>()
                val myDriveAudio = mutableListOf<DriveAudioItem>()
                val sharedFolders = mutableListOf<DriveFolderItem>()
                val sharedAudio = mutableListOf<DriveAudioItem>()
                val sharedDrives = mutableListOf<DriveFolderItem>()
                var rootNextPageToken: String? = null

                // A. Query My Drive root items
                try {
                    val myDriveQuery = DriveQueryBuilder.buildQueryForFolder("root")
                    val myDriveList = drive.files().list()
                        .setQ(myDriveQuery)
                        .setSpaces("drive")
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension, shared, driveId)")
                        .setOrderBy("folder, name_natural")
                        .setPageSize(pageSize)
                        .execute()

                    rootNextPageToken = myDriveList.nextPageToken

                    for (file in myDriveList.files ?: emptyList()) {
                        if (file.mimeType == DriveQueryBuilder.FOLDER_MIME_TYPE) {
                            myDriveFolders.add(
                                DriveFolderItem(
                                    id = file.id,
                                    name = file.name ?: "Untitled Folder",
                                    parentId = file.parents?.firstOrNull() ?: "root",
                                    modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                    driveId = file.driveId,
                                    sourceType = DriveSourceType.MY_DRIVE,
                                    isShared = file.shared ?: false
                                )
                            )
                        } else if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                            val sizeBytes = file.getSize() ?: 0L
                            myDriveAudio.add(
                                DriveAudioItem(
                                    id = file.id,
                                    name = file.name ?: "Unknown Audio",
                                    mimeType = file.mimeType ?: "audio/mpeg",
                                    sizeBytes = sizeBytes,
                                    sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                    formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                    modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                    parentFolderId = file.parents?.firstOrNull() ?: "root",
                                    webViewLink = file.webViewLink,
                                    iconLink = file.iconLink,
                                    driveId = file.driveId,
                                    sourceType = DriveSourceType.MY_DRIVE,
                                    isShared = file.shared ?: false
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Log and continue to fetch shared items
                }

                // B. Query "Shared with me" items
                try {
                    val sharedQuery = DriveQueryBuilder.buildSharedWithMeQuery()
                    val sharedList = drive.files().list()
                        .setQ(sharedQuery)
                        .setSpaces("drive")
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setFields("files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension, shared, driveId)")
                        .setOrderBy("folder, name_natural")
                        .setPageSize(50)
                        .execute()

                    for (file in sharedList.files ?: emptyList()) {
                        if (file.mimeType == DriveQueryBuilder.FOLDER_MIME_TYPE) {
                            sharedFolders.add(
                                DriveFolderItem(
                                    id = file.id,
                                    name = file.name ?: "Shared Folder",
                                    parentId = file.parents?.firstOrNull() ?: "sharedWithMe",
                                    modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                    driveId = file.driveId,
                                    sourceType = DriveSourceType.SHARED_WITH_ME,
                                    isShared = true
                                )
                            )
                        } else if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                            val sizeBytes = file.getSize() ?: 0L
                            sharedAudio.add(
                                DriveAudioItem(
                                    id = file.id,
                                    name = file.name ?: "Shared Audio",
                                    mimeType = file.mimeType ?: "audio/mpeg",
                                    sizeBytes = sizeBytes,
                                    sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                    formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                    modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                    parentFolderId = file.parents?.firstOrNull() ?: "sharedWithMe",
                                    webViewLink = file.webViewLink,
                                    iconLink = file.iconLink,
                                    driveId = file.driveId,
                                    sourceType = DriveSourceType.SHARED_WITH_ME,
                                    isShared = true
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Shared with me query exception handled gracefully
                }

                // C. Query Shared Drives
                try {
                    val drivesList = drive.drives().list()
                        .setPageSize(50)
                        .execute()

                    for (d in drivesList.drives ?: emptyList()) {
                        sharedDrives.add(
                            DriveFolderItem(
                                id = d.id,
                                name = d.name ?: "Shared Drive",
                                parentId = "root",
                                modifiedTime = "",
                                driveId = d.id,
                                sourceType = DriveSourceType.SHARED_DRIVE,
                                isShared = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Shared Drives might not be enabled for personal accounts; ignore gracefully
                }

                // Deduplicate by ID
                val dedupedMyDriveFolders = myDriveFolders.distinctBy { it.id }
                val dedupedMyDriveAudio = myDriveAudio.distinctBy { it.id }
                val dedupedSharedFolders = sharedFolders.distinctBy { it.id }
                val dedupedSharedAudio = sharedAudio.distinctBy { it.id }
                val dedupedSharedDrives = sharedDrives.distinctBy { it.id }

                DriveResult.Success(
                    DrivePageResult(
                        folders = dedupedMyDriveFolders,
                        audioFiles = dedupedMyDriveAudio,
                        sharedFolders = dedupedSharedFolders,
                        sharedAudioFiles = dedupedSharedAudio,
                        sharedDrives = dedupedSharedDrives,
                        nextPageToken = rootNextPageToken,
                        currentFolderId = "root",
                        currentFolderName = folderName,
                        isRoot = true
                    )
                )
            } else {
                // -------------------------------------------------------------
                // 2. SUBFOLDER OR PAGINATED LEVEL
                // -------------------------------------------------------------
                val targetId = if (isRoot) "root" else folderId!!
                val query = DriveQueryBuilder.buildQueryForFolder(targetId)

                val fileList = drive.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension, shared, driveId)")
                    .setOrderBy("folder, name_natural")
                    .setPageSize(pageSize)
                    .apply {
                        if (!pageToken.isNullOrEmpty()) {
                            setPageToken(pageToken)
                        }
                    }
                    .execute()

                val files = fileList.files ?: emptyList()
                val folders = mutableListOf<DriveFolderItem>()
                val audioFiles = mutableListOf<DriveAudioItem>()

                for (file in files) {
                    if (file.mimeType == DriveQueryBuilder.FOLDER_MIME_TYPE) {
                        folders.add(
                            DriveFolderItem(
                                id = file.id,
                                name = file.name ?: "Untitled Folder",
                                parentId = file.parents?.firstOrNull() ?: targetId,
                                modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                driveId = file.driveId,
                                sourceType = if (file.shared == true) DriveSourceType.SHARED_WITH_ME else DriveSourceType.MY_DRIVE,
                                isShared = file.shared ?: false
                            )
                        )
                    } else if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                        val sizeBytes = file.getSize() ?: 0L
                        audioFiles.add(
                            DriveAudioItem(
                                id = file.id,
                                name = file.name ?: "Unknown Audio",
                                mimeType = file.mimeType ?: "audio/mpeg",
                                sizeBytes = sizeBytes,
                                sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                parentFolderId = file.parents?.firstOrNull() ?: targetId,
                                webViewLink = file.webViewLink,
                                iconLink = file.iconLink,
                                driveId = file.driveId,
                                sourceType = if (file.shared == true) DriveSourceType.SHARED_WITH_ME else DriveSourceType.MY_DRIVE,
                                isShared = file.shared ?: false
                            )
                        )
                    }
                }

                DriveResult.Success(
                    DrivePageResult(
                        folders = folders.distinctBy { it.id },
                        audioFiles = audioFiles.distinctBy { it.id },
                        sharedFolders = emptyList(),
                        sharedAudioFiles = emptyList(),
                        sharedDrives = emptyList(),
                        nextPageToken = fileList.nextPageToken,
                        currentFolderId = targetId,
                        currentFolderName = folderName,
                        isRoot = false
                    )
                )
            }
        } catch (e: GoogleJsonResponseException) {
            DriveResult.Error("Google Drive API error (${e.statusCode}): ${e.details?.message ?: e.message}")
        } catch (e: UnknownHostException) {
            DriveResult.Error("No internet connection. Please check your network.", isOffline = true)
        } catch (e: SocketTimeoutException) {
            DriveResult.Error("Google Drive connection timed out. Tap to retry.", isOffline = true)
        } catch (e: IOException) {
            DriveResult.Error("Network error connecting to Google Drive: ${e.localizedMessage ?: "Unknown error"}", isOffline = true)
        } catch (e: Exception) {
            DriveResult.Error("Failed to load Drive contents: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun getStorageQuota(): DriveResult<DriveStorageQuota> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            val about = drive.about().get().setFields("user, storageQuota").execute()
            val quota = about.storageQuota
            if (quota != null) {
                val usageBytes = quota.usage ?: 0L
                val limitBytes = quota.limit ?: 0L
                DriveResult.Success(
                    DriveStorageQuota(
                        usageBytes = usageBytes,
                        limitBytes = limitBytes,
                        usageFormatted = DriveQueryBuilder.formatBytes(usageBytes),
                        limitFormatted = if (limitBytes > 0L) DriveQueryBuilder.formatBytes(limitBytes) else "Unlimited",
                        usagePercent = if (limitBytes > 0L) ((usageBytes.toDouble() / limitBytes.toDouble()) * 100).toFloat() else 0f
                    )
                )
            } else {
                DriveResult.Error("Could not retrieve storage quota.")
            }
        } catch (e: Exception) {
            DriveResult.Error("Failed to get storage quota: ${e.localizedMessage}")
        }
    }

    override suspend fun downloadFile(
        fileId: String,
        destinationFile: File,
        totalSizeBytes: Long,
        onProgress: (Int) -> Unit
    ): DriveResult<File> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            val parentDir = destinationFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
            if (tempFile.exists()) tempFile.delete()

            val inputStream = drive.files().get(fileId)
                .setSupportsAllDrives(true)
                .executeMediaAsInputStream()

            val outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            var totalBytesRead = 0L

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (totalSizeBytes > 0) {
                            val progress = ((totalBytesRead * 100) / totalSizeBytes).toInt().coerceIn(0, 100)
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                if (destinationFile.exists()) destinationFile.delete()
                val renamed = tempFile.renameTo(destinationFile)
                if (!renamed) {
                    tempFile.copyTo(destinationFile, overwrite = true)
                    tempFile.delete()
                }
                onProgress(100)
                DriveResult.Success(destinationFile)
            } else {
                DriveResult.Error("Download failed: empty response file received.")
            }
        } catch (e: GoogleJsonResponseException) {
            DriveResult.Error("Drive API download error (${e.statusCode}): ${e.details?.message ?: e.message}")
        } catch (e: UnknownHostException) {
            DriveResult.Error("You're offline. Connect to the internet to download audio.", isOffline = true)
        } catch (e: SocketTimeoutException) {
            DriveResult.Error("Download connection timed out. Tap to retry.", isOffline = true)
        } catch (e: IOException) {
            DriveResult.Error("Network error during download: ${e.localizedMessage ?: "Check connection"}", isOffline = true)
        } catch (e: Exception) {
            DriveResult.Error("Unexpected error downloading file: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun getAllAudioFilesInFolder(
        folderId: String?
    ): DriveResult<List<DriveAudioItem>> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            val allAudios = LinkedHashMap<String, DriveAudioItem>()

            if (folderId.isNullOrEmpty() || folderId == "root") {
                // Discover ALL accessible audio files across My Drive, Shared With Me, and Shared Drives
                val query = DriveQueryBuilder.buildAllAudioQuery()
                var pageToken: String? = null

                do {
                    val fileList = drive.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setSupportsAllDrives(true)
                        .setIncludeItemsFromAllDrives(true)
                        .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension, shared, driveId)")
                        .setPageSize(100)
                        .apply {
                            if (!pageToken.isNullOrEmpty()) setPageToken(pageToken)
                        }
                        .execute()

                    for (file in fileList.files ?: emptyList()) {
                        if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                            val sizeBytes = file.getSize() ?: 0L
                            val item = DriveAudioItem(
                                id = file.id,
                                name = file.name ?: "Unknown Audio",
                                mimeType = file.mimeType ?: "audio/mpeg",
                                sizeBytes = sizeBytes,
                                sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                parentFolderId = file.parents?.firstOrNull() ?: "root",
                                webViewLink = file.webViewLink,
                                iconLink = file.iconLink,
                                driveId = file.driveId,
                                sourceType = if (file.shared == true) DriveSourceType.SHARED_WITH_ME else DriveSourceType.MY_DRIVE,
                                isShared = file.shared ?: false
                            )
                            allAudios[file.id] = item
                        }
                    }
                    pageToken = fileList.nextPageToken
                } while (!pageToken.isNullOrEmpty())

            } else {
                // Recursively fetch all audio files in the folder hierarchy
                val folderQueue = ArrayDeque<String>()
                folderQueue.add(folderId)

                while (folderQueue.isNotEmpty()) {
                    val currentId = folderQueue.removeFirst()
                    val query = DriveQueryBuilder.buildQueryForFolder(currentId)
                    var pageToken: String? = null

                    do {
                        val fileList = drive.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setSupportsAllDrives(true)
                            .setIncludeItemsFromAllDrives(true)
                            .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension, shared, driveId)")
                            .setPageSize(100)
                            .apply {
                                if (!pageToken.isNullOrEmpty()) setPageToken(pageToken)
                            }
                            .execute()

                        for (file in fileList.files ?: emptyList()) {
                            if (file.mimeType == DriveQueryBuilder.FOLDER_MIME_TYPE) {
                                folderQueue.add(file.id)
                            } else if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                                val sizeBytes = file.getSize() ?: 0L
                                val item = DriveAudioItem(
                                    id = file.id,
                                    name = file.name ?: "Unknown Audio",
                                    mimeType = file.mimeType ?: "audio/mpeg",
                                    sizeBytes = sizeBytes,
                                    sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                    formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                    modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                    parentFolderId = file.parents?.firstOrNull() ?: currentId,
                                    webViewLink = file.webViewLink,
                                    iconLink = file.iconLink,
                                    driveId = file.driveId,
                                    sourceType = if (file.shared == true) DriveSourceType.SHARED_WITH_ME else DriveSourceType.MY_DRIVE,
                                    isShared = file.shared ?: false
                                )
                                allAudios[file.id] = item
                            }
                        }
                        pageToken = fileList.nextPageToken
                    } while (!pageToken.isNullOrEmpty())
                }
            }

            DriveResult.Success(allAudios.values.toList())
        } catch (e: Exception) {
            DriveResult.Error("Could not retrieve audio files: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun createFolder(
        folderName: String,
        parentFolderId: String?
    ): DriveResult<DriveFolderItem> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                if (!parentFolderId.isNullOrEmpty() && parentFolderId != "root" && parentFolderId != "sharedWithMe") {
                    parents = listOf(parentFolderId)
                }
            }

            val created = drive.files().create(folderMetadata)
                .setSupportsAllDrives(true)
                .setFields("id, name, parents, modifiedTime, driveId")
                .execute()

            DriveResult.Success(
                DriveFolderItem(
                    id = created.id,
                    name = created.name ?: folderName,
                    parentId = created.parents?.firstOrNull() ?: parentFolderId ?: "root",
                    modifiedTime = created.modifiedTime?.toStringRfc3339() ?: "",
                    driveId = created.driveId,
                    sourceType = DriveSourceType.MY_DRIVE,
                    isShared = false
                )
            )
        } catch (e: Exception) {
            DriveResult.Error("Failed to create folder \"$folderName\": ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun uploadAudioFile(
        file: File,
        fileName: String,
        mimeType: String,
        parentFolderId: String?,
        onProgress: (Int) -> Unit
    ): DriveResult<DriveAudioItem> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            onProgress(10)
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                if (!parentFolderId.isNullOrEmpty() && parentFolderId != "root" && parentFolderId != "sharedWithMe") {
                    parents = listOf(parentFolderId)
                }
            }

            val mediaContent = FileContent(mimeType, file)
            onProgress(30)
            val uploaded = drive.files().create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, driveId")
                .execute()
            onProgress(100)

            val sizeBytes = uploaded.getSize() ?: file.length()
            DriveResult.Success(
                DriveAudioItem(
                    id = uploaded.id,
                    name = uploaded.name ?: fileName,
                    mimeType = uploaded.mimeType ?: mimeType,
                    sizeBytes = sizeBytes,
                    sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                    formatExtension = DriveQueryBuilder.getFormatExtension(uploaded.name ?: fileName, uploaded.mimeType ?: mimeType),
                    modifiedTime = uploaded.modifiedTime?.toStringRfc3339() ?: "",
                    parentFolderId = uploaded.parents?.firstOrNull() ?: parentFolderId ?: "root",
                    webViewLink = uploaded.webViewLink,
                    iconLink = uploaded.iconLink,
                    driveId = uploaded.driveId,
                    sourceType = DriveSourceType.MY_DRIVE,
                    isShared = false
                )
            )
        } catch (e: Exception) {
            DriveResult.Error("Failed to upload \"$fileName\" to Google Drive: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun findOrCreateDefaultMusicFolder(): DriveResult<DriveFolderItem> = withContext(Dispatchers.IO) {
        val drive = getDriveClient() ?: return@withContext DriveResult.Error("Authentication required.", isAuthExpired = true)
        try {
            val query = "mimeType = 'application/vnd.google-apps.folder' and name = 'DriveTune Music' and trashed = false"
            val fileList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setFields("files(id, name, parents, modifiedTime, driveId)")
                .setPageSize(5)
                .execute()

            val existing = fileList.files?.firstOrNull()
            if (existing != null) {
                DriveResult.Success(
                    DriveFolderItem(
                        id = existing.id,
                        name = existing.name ?: "DriveTune Music",
                        parentId = existing.parents?.firstOrNull() ?: "root",
                        modifiedTime = existing.modifiedTime?.toStringRfc3339() ?: "",
                        driveId = existing.driveId,
                        sourceType = DriveSourceType.MY_DRIVE,
                        isShared = false
                    )
                )
            } else {
                createFolder("DriveTune Music")
            }
        } catch (e: Exception) {
            DriveResult.Error("Could not find or create DriveTune Music folder: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}

