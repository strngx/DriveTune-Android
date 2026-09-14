package com.drivetune.app.data

import com.drivetune.app.model.Album
import com.drivetune.app.model.DriveFolder
import com.drivetune.app.model.Playlist
import com.drivetune.app.model.Track
import com.drivetune.app.model.TrackDownloadStatus

object MockDataProvider {

    val sampleFolders = listOf(
        DriveFolder(
            id = "f-synthwave",
            name = "Synthwave & Retrowave [FLAC]",
            path = "/Google Drive/Music/Synthwave",
            trackCount = 6,
            sizeString = "284 MB"
        ),
        DriveFolder(
            id = "f-lofi",
            name = "Late Night Lo-Fi Beats",
            path = "/Google Drive/Music/Lo-Fi",
            trackCount = 5,
            sizeString = "142 MB"
        ),
        DriveFolder(
            id = "f-ambient",
            name = "Deep Focus & Ambient Space",
            path = "/Google Drive/Music/Ambient",
            trackCount = 4,
            sizeString = "198 MB"
        ),
        DriveFolder(
            id = "f-acoustic",
            name = "Acoustic Sessions 2024",
            path = "/Google Drive/Music/Acoustic",
            trackCount = 5,
            sizeString = "165 MB"
        )
    )

    val sampleTracks = listOf(
        Track(
            id = "trk-101",
            title = "Neon Horizon",
            artist = "Kavinsky Wave",
            album = "Cyber Odyssey",
            albumId = "alb-1",
            folderId = "f-synthwave",
            durationSeconds = 218,
            format = "FLAC",
            sizeString = "42.4 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80"
        ),
        Track(
            id = "trk-102",
            title = "Midnight Highway",
            artist = "Kavinsky Wave",
            album = "Cyber Odyssey",
            albumId = "alb-1",
            folderId = "f-synthwave",
            durationSeconds = 254,
            format = "FLAC",
            sizeString = "48.1 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80"
        ),
        Track(
            id = "trk-103",
            title = "Digital Sunset",
            artist = "Kavinsky Wave",
            album = "Cyber Odyssey",
            albumId = "alb-1",
            folderId = "f-synthwave",
            durationSeconds = 195,
            format = "FLAC",
            sizeString = "39.0 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80"
        ),
        Track(
            id = "trk-104",
            title = "Starfall Velocity",
            artist = "Solaris Core",
            album = "Starlight Drive",
            albumId = "alb-2",
            folderId = "f-synthwave",
            durationSeconds = 240,
            format = "FLAC",
            sizeString = "45.2 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80"
        ),
        Track(
            id = "trk-105",
            title = "Electric Rain",
            artist = "Solaris Core",
            album = "Starlight Drive",
            albumId = "alb-2",
            folderId = "f-synthwave",
            durationSeconds = 210,
            format = "FLAC",
            sizeString = "41.8 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80"
        ),
        Track(
            id = "trk-106",
            title = "Chrome Reverie",
            artist = "Solaris Core",
            album = "Starlight Drive",
            albumId = "alb-2",
            folderId = "f-synthwave",
            durationSeconds = 275,
            format = "FLAC",
            sizeString = "52.0 MB",
            bitrate = "1411 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80"
        ),
        Track(
            id = "trk-201",
            title = "Tokyo Rain Drops",
            artist = "Aura Chill",
            album = "Rainy Cafe Moments",
            albumId = "alb-3",
            folderId = "f-lofi",
            durationSeconds = 168,
            format = "MP3",
            sizeString = "8.4 MB",
            bitrate = "320 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80"
        ),
        Track(
            id = "trk-202",
            title = "Midnight Coffee",
            artist = "Aura Chill",
            album = "Rainy Cafe Moments",
            albumId = "alb-3",
            folderId = "f-lofi",
            durationSeconds = 182,
            format = "MP3",
            sizeString = "9.1 MB",
            bitrate = "320 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80"
        ),
        Track(
            id = "trk-203",
            title = "Study Window Breeze",
            artist = "Aura Chill",
            album = "Rainy Cafe Moments",
            albumId = "alb-3",
            folderId = "f-lofi",
            durationSeconds = 154,
            format = "MP3",
            sizeString = "7.8 MB",
            bitrate = "320 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80"
        ),
        Track(
            id = "trk-301",
            title = "Orbit Resonance",
            artist = "Celestial Void",
            album = "Deep Cosmos IV",
            albumId = "alb-4",
            folderId = "f-ambient",
            durationSeconds = 340,
            format = "WAV",
            sizeString = "62.5 MB",
            bitrate = "1536 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80"
        ),
        Track(
            id = "trk-302",
            title = "Nebula Dreamscape",
            artist = "Celestial Void",
            album = "Deep Cosmos IV",
            albumId = "alb-4",
            folderId = "f-ambient",
            durationSeconds = 312,
            format = "WAV",
            sizeString = "58.0 MB",
            bitrate = "1536 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80"
        ),
        Track(
            id = "trk-401",
            title = "Campfire Harmonics",
            artist = "Oak & Pine",
            album = "Mountain Air Sessions",
            albumId = "alb-5",
            folderId = "f-acoustic",
            durationSeconds = 204,
            format = "M4A",
            sizeString = "14.2 MB",
            bitrate = "256 kbps",
            status = TrackDownloadStatus.DOWNLOADED,
            downloadProgress = 100,
            coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80"
        ),
        Track(
            id = "trk-402",
            title = "Valley Echoes",
            artist = "Oak & Pine",
            album = "Mountain Air Sessions",
            albumId = "alb-5",
            folderId = "f-acoustic",
            durationSeconds = 230,
            format = "M4A",
            sizeString = "15.8 MB",
            bitrate = "256 kbps",
            status = TrackDownloadStatus.CLOUD,
            downloadProgress = 0,
            coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80"
        )
    )

    val sampleAlbums = listOf(
        Album(
            id = "alb-1",
            title = "Cyber Odyssey",
            artist = "Kavinsky Wave",
            year = "2024",
            folderId = "f-synthwave",
            folderPath = "/Google Drive/Music/Synthwave",
            coverArtUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80",
            trackIds = listOf("trk-101", "trk-102", "trk-103")
        ),
        Album(
            id = "alb-2",
            title = "Starlight Drive",
            artist = "Solaris Core",
            year = "2023",
            folderId = "f-synthwave",
            folderPath = "/Google Drive/Music/Synthwave",
            coverArtUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80",
            trackIds = listOf("trk-104", "trk-105", "trk-106")
        ),
        Album(
            id = "alb-3",
            title = "Rainy Cafe Moments",
            artist = "Aura Chill",
            year = "2024",
            folderId = "f-lofi",
            folderPath = "/Google Drive/Music/Lo-Fi",
            coverArtUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80",
            trackIds = listOf("trk-201", "trk-202", "trk-203")
        ),
        Album(
            id = "alb-4",
            title = "Deep Cosmos IV",
            artist = "Celestial Void",
            year = "2022",
            folderId = "f-ambient",
            folderPath = "/Google Drive/Music/Ambient",
            coverArtUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80",
            trackIds = listOf("trk-301", "trk-302")
        ),
        Album(
            id = "alb-5",
            title = "Mountain Air Sessions",
            artist = "Oak & Pine",
            year = "2024",
            folderId = "f-acoustic",
            folderPath = "/Google Drive/Music/Acoustic",
            coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80",
            trackIds = listOf("trk-401", "trk-402")
        )
    )

    val samplePlaylists = listOf(
        Playlist(
            id = "pl-1",
            title = "Road Trip Essentials",
            description = "High tempo synthwave and energetic offline bops",
            trackIds = listOf("trk-101", "trk-102", "trk-401", "trk-201"),
            coverArtUrls = listOf(
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80",
                "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=200&q=80",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80"
            )
        ),
        Playlist(
            id = "pl-2",
            title = "Focus & Coding Flow",
            description = "Ambient space drones and lo-fi chill",
            trackIds = listOf("trk-201", "trk-202", "trk-301"),
            coverArtUrls = listOf(
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80",
                "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=200&q=80",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80"
            )
        )
    )
}
