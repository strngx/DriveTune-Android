/**
 * DriveTune Initial Mock Data & Metadata
 */

export const INITIAL_DRIVE_FOLDERS = [
  {
    id: 'f-synthwave',
    name: 'Synthwave & Retrowave [FLAC]',
    path: '/Google Drive/Music/Synthwave',
    trackCount: 6,
    size: '284 MB',
    color: '#8B5CF6'
  },
  {
    id: 'f-lofi',
    name: 'Late Night Lo-Fi Beats',
    path: '/Google Drive/Music/Lo-Fi',
    trackCount: 5,
    size: '142 MB',
    color: '#EC4899'
  },
  {
    id: 'f-ambient',
    name: 'Deep Focus & Ambient Space',
    path: '/Google Drive/Music/Ambient',
    trackCount: 4,
    size: '198 MB',
    color: '#3B82F6'
  },
  {
    id: 'f-acoustic',
    name: 'Acoustic Sessions 2024',
    path: '/Google Drive/Music/Acoustic',
    trackCount: 5,
    size: '165 MB',
    color: '#10B981'
  }
];

export const INITIAL_TRACKS = [
  {
    id: 'trk-101',
    title: 'Neon Horizon',
    artist: 'Kavinsky Wave',
    album: 'Cyber Odyssey',
    albumId: 'alb-1',
    folderId: 'f-synthwave',
    duration: 218, // 3:38
    durationStr: '3:38',
    format: 'FLAC',
    size: '42.4 MB',
    bitrate: '1411 kbps',
    status: 'downloaded', // 'cloud' | 'downloading' | 'downloaded' | 'failed'
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80',
    audioFreq: 440,
    bpm: 110
  },
  {
    id: 'trk-102',
    title: 'Midnight Highway',
    artist: 'Kavinsky Wave',
    album: 'Cyber Odyssey',
    albumId: 'alb-1',
    folderId: 'f-synthwave',
    duration: 254, // 4:14
    durationStr: '4:14',
    format: 'FLAC',
    size: '48.1 MB',
    bitrate: '1411 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80',
    audioFreq: 330,
    bpm: 115
  },
  {
    id: 'trk-103',
    title: 'Digital Sunset',
    artist: 'Kavinsky Wave',
    album: 'Cyber Odyssey',
    albumId: 'alb-1',
    folderId: 'f-synthwave',
    duration: 195, // 3:15
    durationStr: '3:15',
    format: 'FLAC',
    size: '39.0 MB',
    bitrate: '1411 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80',
    audioFreq: 520,
    bpm: 120
  },
  {
    id: 'trk-104',
    title: 'Starfall Velocity',
    artist: 'Solaris Core',
    album: 'Starlight Drive',
    albumId: 'alb-2',
    folderId: 'f-synthwave',
    duration: 240, // 4:00
    durationStr: '4:00',
    format: 'FLAC',
    size: '45.2 MB',
    bitrate: '1411 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80',
    audioFreq: 392,
    bpm: 124
  },
  {
    id: 'trk-105',
    title: 'Electric Rain',
    artist: 'Solaris Core',
    album: 'Starlight Drive',
    albumId: 'alb-2',
    folderId: 'f-synthwave',
    duration: 210, // 3:30
    durationStr: '3:30',
    format: 'FLAC',
    size: '41.8 MB',
    bitrate: '1411 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80',
    audioFreq: 349,
    bpm: 108
  },
  {
    id: 'trk-106',
    title: 'Chrome Reverie',
    artist: 'Solaris Core',
    album: 'Starlight Drive',
    albumId: 'alb-2',
    folderId: 'f-synthwave',
    duration: 275, // 4:35
    durationStr: '4:35',
    format: 'FLAC',
    size: '52.0 MB',
    bitrate: '1411 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80',
    audioFreq: 440,
    bpm: 112
  },
  {
    id: 'trk-201',
    title: 'Tokyo Rain Drops',
    artist: 'Aura Chill',
    album: 'Rainy Cafe Moments',
    albumId: 'alb-3',
    folderId: 'f-lofi',
    duration: 168, // 2:48
    durationStr: '2:48',
    format: 'MP3',
    size: '8.4 MB',
    bitrate: '320 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80',
    audioFreq: 261,
    bpm: 80
  },
  {
    id: 'trk-202',
    title: 'Midnight Coffee',
    artist: 'Aura Chill',
    album: 'Rainy Cafe Moments',
    albumId: 'alb-3',
    folderId: 'f-lofi',
    duration: 182, // 3:02
    durationStr: '3:02',
    format: 'MP3',
    size: '9.1 MB',
    bitrate: '320 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80',
    audioFreq: 293,
    bpm: 78
  },
  {
    id: 'trk-203',
    title: 'Study Window Breeze',
    artist: 'Aura Chill',
    album: 'Rainy Cafe Moments',
    albumId: 'alb-3',
    folderId: 'f-lofi',
    duration: 154, // 2:34
    durationStr: '2:34',
    format: 'MP3',
    size: '7.8 MB',
    bitrate: '320 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80',
    audioFreq: 329,
    bpm: 82
  },
  {
    id: 'trk-301',
    title: 'Orbit Resonance',
    artist: 'Celestial Void',
    album: 'Deep Cosmos IV',
    albumId: 'alb-4',
    folderId: 'f-ambient',
    duration: 340, // 5:40
    durationStr: '5:40',
    format: 'WAV',
    size: '62.5 MB',
    bitrate: '1536 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80',
    audioFreq: 220,
    bpm: 60
  },
  {
    id: 'trk-302',
    title: 'Nebula Dreamscape',
    artist: 'Celestial Void',
    album: 'Deep Cosmos IV',
    albumId: 'alb-4',
    folderId: 'f-ambient',
    duration: 312, // 5:12
    durationStr: '5:12',
    format: 'WAV',
    size: '58.0 MB',
    bitrate: '1536 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80',
    audioFreq: 246,
    bpm: 65
  },
  {
    id: 'trk-401',
    title: 'Campfire Harmonics',
    artist: 'Oak & Pine',
    album: 'Mountain Air Sessions',
    albumId: 'alb-5',
    folderId: 'f-acoustic',
    duration: 204, // 3:24
    durationStr: '3:24',
    format: 'M4A',
    size: '14.2 MB',
    bitrate: '256 kbps',
    status: 'downloaded',
    progress: 100,
    coverArt: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80',
    audioFreq: 370,
    bpm: 95
  },
  {
    id: 'trk-402',
    title: 'Valley Echoes',
    artist: 'Oak & Pine',
    album: 'Mountain Air Sessions',
    albumId: 'alb-5',
    folderId: 'f-acoustic',
    duration: 230, // 3:50
    durationStr: '3:50',
    format: 'M4A',
    size: '15.8 MB',
    bitrate: '256 kbps',
    status: 'cloud',
    progress: 0,
    coverArt: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80',
    audioFreq: 415,
    bpm: 92
  }
];

export const INITIAL_ALBUMS = [
  {
    id: 'alb-1',
    title: 'Cyber Odyssey',
    artist: 'Kavinsky Wave',
    year: '2024',
    folderId: 'f-synthwave',
    folderPath: '/Google Drive/Music/Synthwave',
    coverArt: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80',
    trackIds: ['trk-101', 'trk-102', 'trk-103']
  },
  {
    id: 'alb-2',
    title: 'Starlight Drive',
    artist: 'Solaris Core',
    year: '2023',
    folderId: 'f-synthwave',
    folderPath: '/Google Drive/Music/Synthwave',
    coverArt: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80',
    trackIds: ['trk-104', 'trk-105', 'trk-106']
  },
  {
    id: 'alb-3',
    title: 'Rainy Cafe Moments',
    artist: 'Aura Chill',
    year: '2024',
    folderId: 'f-lofi',
    folderPath: '/Google Drive/Music/Lo-Fi',
    coverArt: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80',
    trackIds: ['trk-201', 'trk-202', 'trk-203']
  },
  {
    id: 'alb-4',
    title: 'Deep Cosmos IV',
    artist: 'Celestial Void',
    year: '2022',
    folderId: 'f-ambient',
    folderPath: '/Google Drive/Music/Ambient',
    coverArt: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400&q=80',
    trackIds: ['trk-301', 'trk-302']
  },
  {
    id: 'alb-5',
    title: 'Mountain Air Sessions',
    artist: 'Oak & Pine',
    year: '2024',
    folderId: 'f-acoustic',
    folderPath: '/Google Drive/Music/Acoustic',
    coverArt: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80',
    trackIds: ['trk-401', 'trk-402']
  }
];

export const INITIAL_PLAYLISTS = [
  {
    id: 'pl-1',
    title: 'Road Trip Essentials',
    description: 'High tempo synthwave and energetic offline bops',
    trackIds: ['trk-101', 'trk-102', 'trk-401', 'trk-201'],
    covers: [
      'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80',
      'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=200&q=80',
      'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80',
      'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80'
    ]
  },
  {
    id: 'pl-2',
    title: 'Focus & Coding Flow',
    description: 'Ambient space drones and lo-fi chill',
    trackIds: ['trk-201', 'trk-202', 'trk-301'],
    covers: [
      'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80',
      'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=200&q=80',
      'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80',
      'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80'
    ]
  }
];
