package com.drivetune.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.drivetune.app.drive.DriveAudioItem
import com.drivetune.app.model.DriveFolder
import com.drivetune.app.model.TrackDownloadStatus
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.AccentMintDim
import com.drivetune.app.theme.AccentText
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderMedium
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary
import com.drivetune.app.ui.components.DriveAudioRow
import com.drivetune.app.ui.components.DriveEmptyView
import com.drivetune.app.ui.components.DriveErrorView
import com.drivetune.app.ui.components.DriveLoadingView
import com.drivetune.app.ui.components.FolderCard
import com.drivetune.app.ui.components.OfflineBanner
import com.drivetune.app.viewmodel.MainUiState

@Composable
fun DriveBrowserScreen(
    state: MainUiState,
    onFolderClick: (String, String) -> Unit,
    onBackToParentFolder: () -> Unit,
    onRefreshDrive: () -> Unit,
    onSaveOfflineFile: (DriveAudioItem) -> Unit,
    onDownloadFolder: () -> Unit,
    onToggleSelectFile: (String) -> Unit,
    onSelectAllFiles: () -> Unit,
    onClearFileSelection: () -> Unit,
    onDownloadSelectedFiles: () -> Unit,
    onLoadNextPage: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val isInsideSubfolder = state.isInsideSubfolder

    val filteredMyDriveFolders = remember(state.driveFolders, searchQuery) {
        if (searchQuery.isBlank()) state.driveFolders
        else state.driveFolders.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val filteredMyDriveAudio = remember(state.driveAudioFiles, searchQuery) {
        if (searchQuery.isBlank()) state.driveAudioFiles
        else state.driveAudioFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val filteredSharedFolders = remember(state.sharedFolders, searchQuery) {
        if (searchQuery.isBlank()) state.sharedFolders
        else state.sharedFolders.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val filteredSharedAudio = remember(state.sharedAudioFiles, searchQuery) {
        if (searchQuery.isBlank()) state.sharedAudioFiles
        else state.sharedAudioFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val filteredSharedDrives = remember(state.sharedDrives, searchQuery) {
        if (searchQuery.isBlank()) state.sharedDrives
        else state.sharedDrives.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google Drive",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isInsideSubfolder) state.currentFolderTitle else "Browse & Save Offline",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = if (isInsideSubfolder) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshDrive,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BgSurface2)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(2.dp, BorderMedium, CircleShape)
                            .clickable(onClick = onProfileClick)
                    ) {
                        val avatar = state.authenticatedUser?.avatarUrl
                        if (!avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatar,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(BgSurface3),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.authenticatedUser?.displayName?.take(1)?.uppercase() ?: "G",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentMint,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline banner
            OfflineBanner(isOffline = !state.isOnline)
            if (!state.isOnline) Spacer(modifier = Modifier.height(14.dp))

            // Folder Download Active Progress Banner
            if (state.isFolderDownloading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface2)
                        .border(1.dp, AccentMint.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AccentMint,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Downloading \"${state.activeFolderDownloadName}\"...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${state.folderDownloadCompletedCount} of ${state.folderDownloadTotalCount} files saved offline",
                            fontSize = 11.sp,
                            color = AccentMint
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Subfolder Navigation & Download Folder Action Bar
            if (isInsideSubfolder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBackToParentFolder)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back to previous folder",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentMint
                        )
                    }

                    Button(
                        onClick = onDownloadFolder,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentMintDim, contentColor = AccentMint),
                        border = BorderStroke(1.dp, AccentMint.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download Folder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter music in ${state.currentFolderTitle}...", fontSize = 13.sp, color = TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(BgSurface2),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BgSurface3,
                    unfocusedContainerColor = BgSurface2,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-Select Toolbar
            if (state.selectedDriveFileIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface3)
                        .border(1.dp, AccentMint.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.selectedDriveFileIds.size} selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentMint
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onSelectAllFiles,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("All", fontSize = 11.sp, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = onClearFileSelection,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Clear", fontSize = 11.sp, color = TextSecondary)
                        }
                        Button(
                            onClick = onDownloadSelectedFiles,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint, contentColor = AccentText),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Loading State
        if (state.isDriveLoading) {
            item {
                DriveLoadingView(message = "Loading ${state.currentFolderTitle}...")
            }
        } else if (state.driveError != null) {
            // Error State
            item {
                DriveErrorView(
                    message = state.driveError,
                    onRetryClick = onRefreshDrive
                )
            }
        } else if (state.isDriveEmpty) {
            // Empty State
            item {
                DriveEmptyView(isSubfolder = isInsideSubfolder)
            }
        } else if (isInsideSubfolder) {
            // ==============================================================
            // SUBFOLDER VIEW
            // ==============================================================

            // Subfolders in this folder (if any)
            if (filteredMyDriveFolders.isNotEmpty()) {
                item {
                    Text(
                        text = "Folders (${filteredMyDriveFolders.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredMyDriveFolders) { folderItem ->
                    FolderCard(
                        folder = DriveFolder(
                            id = folderItem.id,
                            name = folderItem.name,
                            path = if (folderItem.isShared) "Shared Folder" else "Google Drive",
                            trackCount = 0,
                            sizeString = "Folder"
                        ),
                        onClick = { onFolderClick(folderItem.id, folderItem.name) },
                        badgeText = if (folderItem.isShared) "SHARED" else null,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Audio Files in this folder (if any)
            if (filteredMyDriveAudio.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Audio Files (${filteredMyDriveAudio.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredMyDriveAudio) { audioItem ->
                    val status = state.downloadStatusByFileId[audioItem.id] ?: TrackDownloadStatus.CLOUD
                    val progress = state.downloadProgressByFileId[audioItem.id] ?: 0

                    DriveAudioRow(
                        item = audioItem,
                        isSelected = state.selectedDriveFileIds.contains(audioItem.id),
                        downloadStatus = status,
                        downloadProgress = progress,
                        onToggleSelect = { onToggleSelectFile(audioItem.id) },
                        onSaveOffline = { onSaveOfflineFile(audioItem) },
                        onClick = { onToggleSelectFile(audioItem.id) },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                // Pagination Load More
                if (state.hasNextDrivePage) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isDrivePaginating) {
                                CircularProgressIndicator(color = AccentMint, modifier = Modifier.size(28.dp))
                            } else {
                                OutlinedButton(
                                    onClick = onLoadNextPage,
                                    shape = CircleShape,
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                ) {
                                    Text("Load More Files", color = AccentMint, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==============================================================
            // ROOT VIEW: CLEAR SECTIONS (MY DRIVE, SHARED WITH ME, SHARED DRIVES)
            // ==============================================================

            // 1. MY DRIVE SECTION
            val hasMyDriveContent = filteredMyDriveFolders.isNotEmpty() || filteredMyDriveAudio.isNotEmpty()
            if (hasMyDriveContent) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY DRIVE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentMint,
                            letterSpacing = 0.5.sp
                        )

                        Button(
                            onClick = onDownloadFolder,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMintDim, contentColor = AccentMint),
                            border = BorderStroke(1.dp, AccentMint.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save All Offline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // My Drive Folders
                items(filteredMyDriveFolders) { folderItem ->
                    FolderCard(
                        folder = DriveFolder(
                            id = folderItem.id,
                            name = folderItem.name,
                            path = "My Drive",
                            trackCount = 0,
                            sizeString = "Folder"
                        ),
                        onClick = { onFolderClick(folderItem.id, folderItem.name) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // My Drive Root Audio Files
                if (filteredMyDriveAudio.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Audio Files (${filteredMyDriveAudio.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(filteredMyDriveAudio) { audioItem ->
                        val status = state.downloadStatusByFileId[audioItem.id] ?: TrackDownloadStatus.CLOUD
                        val progress = state.downloadProgressByFileId[audioItem.id] ?: 0

                        DriveAudioRow(
                            item = audioItem,
                            isSelected = state.selectedDriveFileIds.contains(audioItem.id),
                            downloadStatus = status,
                            downloadProgress = progress,
                            onToggleSelect = { onToggleSelectFile(audioItem.id) },
                            onSaveOffline = { onSaveOfflineFile(audioItem) },
                            onClick = { onToggleSelectFile(audioItem.id) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 2. SHARED WITH ME SECTION
            val hasSharedContent = filteredSharedFolders.isNotEmpty() || filteredSharedAudio.isNotEmpty()
            if (hasSharedContent) {
                item {
                    Text(
                        text = "SHARED WITH ME",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentMint,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Shared Folders
                items(filteredSharedFolders) { folderItem ->
                    FolderCard(
                        folder = DriveFolder(
                            id = folderItem.id,
                            name = folderItem.name,
                            path = "Shared with me",
                            trackCount = 0,
                            sizeString = "Folder"
                        ),
                        onClick = { onFolderClick(folderItem.id, folderItem.name) },
                        badgeText = "SHARED",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Shared Audio Files
                if (filteredSharedAudio.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shared Audio Files (${filteredSharedAudio.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(filteredSharedAudio) { audioItem ->
                        val status = state.downloadStatusByFileId[audioItem.id] ?: TrackDownloadStatus.CLOUD
                        val progress = state.downloadProgressByFileId[audioItem.id] ?: 0

                        DriveAudioRow(
                            item = audioItem,
                            isSelected = state.selectedDriveFileIds.contains(audioItem.id),
                            downloadStatus = status,
                            downloadProgress = progress,
                            onToggleSelect = { onToggleSelectFile(audioItem.id) },
                            onSaveOffline = { onSaveOfflineFile(audioItem) },
                            onClick = { onToggleSelectFile(audioItem.id) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 3. SHARED DRIVES SECTION
            if (filteredSharedDrives.isNotEmpty()) {
                item {
                    Text(
                        text = "SHARED DRIVES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentMint,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredSharedDrives) { driveItem ->
                    FolderCard(
                        folder = DriveFolder(
                            id = driveItem.id,
                            name = driveItem.name,
                            path = "Shared Drive",
                            trackCount = 0,
                            sizeString = "Shared Drive"
                        ),
                        onClick = { onFolderClick(driveItem.id, driveItem.name) },
                        badgeText = "SHARED DRIVE",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Pagination Load More (if root has next page token)
            if (state.hasNextDrivePage) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isDrivePaginating) {
                            CircularProgressIndicator(color = AccentMint, modifier = Modifier.size(28.dp))
                        } else {
                            OutlinedButton(
                                onClick = onLoadNextPage,
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Text("Load More Files", color = AccentMint, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

