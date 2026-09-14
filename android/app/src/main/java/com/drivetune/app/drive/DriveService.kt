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
            val query = DriveQueryBuilder.buildQueryForFolder(folderId)
            val fileList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension)")
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
                            parentId = file.parents?.firstOrNull() ?: folderId,
                            modifiedTime = file.modifiedTime?.toStringRfc3339() ?: ""
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
                            parentFolderId = file.parents?.firstOrNull() ?: folderId,
                            webViewLink = file.webViewLink,
                            iconLink = file.iconLink
                        )
                    )
                }
            }

            DriveResult.Success(
                DrivePageResult(
                    folders = folders,
                    audioFiles = audioFiles,
                    nextPageToken = fileList.nextPageToken,
                    currentFolderId = folderId,
                    currentFolderName = folderName
                )
            )
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

            val inputStream = drive.files().get(fileId).executeMediaAsInputStream()
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
            val query = DriveQueryBuilder.buildQueryForFolder(folderId)
            val allAudios = mutableListOf<DriveAudioItem>()
            var pageToken: String? = null

            do {
                val fileList = drive.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink, fileExtension)")
                    .setPageSize(100)
                    .apply {
                        if (!pageToken.isNullOrEmpty()) setPageToken(pageToken)
                    }
                    .execute()

                val files = fileList.files ?: emptyList()
                for (file in files) {
                    if (DriveQueryBuilder.isAudioFile(file.mimeType, file.name)) {
                        val sizeBytes = file.getSize() ?: 0L
                        allAudios.add(
                            DriveAudioItem(
                                id = file.id,
                                name = file.name ?: "Unknown Audio",
                                mimeType = file.mimeType ?: "audio/mpeg",
                                sizeBytes = sizeBytes,
                                sizeFormatted = DriveQueryBuilder.formatBytes(sizeBytes),
                                formatExtension = DriveQueryBuilder.getFormatExtension(file.name, file.mimeType),
                                modifiedTime = file.modifiedTime?.toStringRfc3339() ?: "",
                                parentFolderId = file.parents?.firstOrNull() ?: folderId,
                                webViewLink = file.webViewLink,
                                iconLink = file.iconLink
                            )
                        )
                    }
                }
                pageToken = fileList.nextPageToken
            } while (!pageToken.isNullOrEmpty())

            DriveResult.Success(allAudios)
        } catch (e: Exception) {
            DriveResult.Error("Could not retrieve audio files in folder: ${e.localizedMessage ?: "Unknown error"}")
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
                if (!parentFolderId.isNullOrEmpty() && parentFolderId != "root") {
                    parents = listOf(parentFolderId)
                }
            }

            val created = drive.files().create(folderMetadata)
                .setFields("id, name, parents, modifiedTime")
                .execute()

            DriveResult.Success(
                DriveFolderItem(
                    id = created.id,
                    name = created.name ?: folderName,
                    parentId = created.parents?.firstOrNull() ?: parentFolderId ?: "root",
                    modifiedTime = created.modifiedTime?.toStringRfc3339() ?: ""
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
                if (!parentFolderId.isNullOrEmpty() && parentFolderId != "root") {
                    parents = listOf(parentFolderId)
                }
            }

            val mediaContent = FileContent(mimeType, file)
            onProgress(30)
            val uploaded = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, size, modifiedTime, parents, webViewLink, iconLink")
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
                    iconLink = uploaded.iconLink
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
                .setFields("files(id, name, parents, modifiedTime)")
                .setPageSize(5)
                .execute()

            val existing = fileList.files?.firstOrNull()
            if (existing != null) {
                DriveResult.Success(
                    DriveFolderItem(
                        id = existing.id,
                        name = existing.name ?: "DriveTune Music",
                        parentId = existing.parents?.firstOrNull() ?: "root",
                        modifiedTime = existing.modifiedTime?.toStringRfc3339() ?: ""
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
