package com.drivetune.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivetune.app.theme.AccentMint
import com.drivetune.app.theme.BgBase
import com.drivetune.app.theme.BgSurface2
import com.drivetune.app.theme.BgSurface3
import com.drivetune.app.theme.BorderSubtle
import com.drivetune.app.theme.DriveBlue
import com.drivetune.app.theme.TextPrimary
import com.drivetune.app.theme.TextSecondary
import com.drivetune.app.theme.TextTertiary

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 60.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BgSurface2)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "DriveTune Data & Privacy Commitments",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Summary Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface2)
                    .border(1.dp, AccentMint.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BgSurface3),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Offline-First & Private",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Your music stays on your device.",
                            fontSize = 12.sp,
                            color = AccentMint
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DriveTune connects directly to your Google Drive to let you browse and download your personal audio files for offline playback, as well as upload user-selected phone audio to your Drive for backup. We do not operate external servers, sell your data, or access files outside your music library.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 1: Google Authentication
        item {
            PolicySectionCard(
                icon = Icons.Default.Lock,
                iconTint = DriveBlue,
                title = "1. Google Authentication & Session",
                content = "• DriveTune uses official Google Sign-In with OAuth 2.0.\n• Your password is never entered in or visible to DriveTune.\n• Authentication tokens are securely saved in Android EncryptedSharedPreferences (AES-256 GCM).\n• You can sign out at any time from Settings to clear the session."
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: Google Drive Permissions & Uploads
        item {
            PolicySectionCard(
                icon = Icons.Default.FolderSpecial,
                iconTint = AccentMint,
                title = "2. Minimum Scopes: Read-Only & App-Created Files",
                content = "• DriveTune requests read-only Drive access to browse music folders, and drive.file access to create folders and upload music that you explicitly add.\n• DriveTune only queries audio MIME types (e.g. MP3, FLAC, WAV, M4A, OGG) and folder structures.\n• When you choose 'Add Music', DriveTune uploads your selected songs to your Google Drive destination folder and retains a local copy for offline playback.\n• DriveTune NEVER deletes, modifies, renames, or overwrites unrelated Drive files.\n• DriveTune never uploads music without your explicit user action."
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: Local Storage & Offline Music
        item {
            PolicySectionCard(
                icon = Icons.Default.CloudDone,
                iconTint = AccentMint,
                title = "3. On-Device Storage",
                content = "• Audio files selected for 'Save Offline' are downloaded directly into DriveTune's private app directory on your device.\n• Downloaded files are managed strictly by DriveTune and are not shared with third-party apps.\n• You can delete downloaded files at any time via 'Clear Downloaded Music' in Settings without affecting your Google Drive originals."
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 4: Zero Tracking & Disconnection
        item {
            PolicySectionCard(
                icon = Icons.Default.SyncDisabled,
                iconTint = TextSecondary,
                title = "4. Zero Analytics & Sign-Out Behavior",
                content = "• DriveTune does not use tracking SDKs, ad networks, or telemetry servers.\n• When you Sign Out, cached Drive browsing states and Google credentials are fully cleared.\n• Previously saved offline music files remain safely on your device for uninterrupted offline listening."
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 5: Security & Contact
        item {
            PolicySectionCard(
                icon = Icons.Default.Security,
                iconTint = DriveBlue,
                title = "5. Security & Inquiries",
                content = "• Communication with Google Drive occurs exclusively over encrypted HTTPS/TLS.\n• If you have questions regarding DriveTune's privacy practices, refer to the project documentation and settings."
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface2)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BgSurface3),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}
