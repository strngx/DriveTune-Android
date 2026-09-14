/**
 * DriveTune Central Reactive State
 */

import { INITIAL_DRIVE_FOLDERS, INITIAL_TRACKS, INITIAL_ALBUMS, INITIAL_PLAYLISTS } from './data.js';

class StateStore {
  constructor() {
    this.state = {
      isAuthenticated: true,
      user: {
        name: 'Alex Vance',
        email: 'alex.vance@gmail.com',
        avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&q=80',
        driveUsedSpace: '4.2 GB',
        driveTotalSpace: '15 GB'
      },
      currentScreen: 'library', // 'auth' | 'library' | 'drive' | 'playlists' | 'settings' | 'album-detail' | 'playlist-detail' | 'sync-queue'
      activeAlbumId: null,
      activePlaylistId: null,
      activeDriveFolderId: null,
      
      // Data Collections
      folders: [...INITIAL_DRIVE_FOLDERS],
      tracks: [...INITIAL_TRACKS],
      albums: [...INITIAL_ALBUMS],
      playlists: [...INITIAL_PLAYLISTS],
      
      // Selection for Multi-Download
      selectedTrackIds: new Set(),

      // Playback State
      currentTrack: INITIAL_TRACKS[0], // Start with first downloaded track
      isPlaying: false,
      currentTime: 0,
      duration: INITIAL_TRACKS[0].duration,
      shuffle: false,
      repeat: 'off', // 'off' | 'all' | 'one'
      queue: [...INITIAL_TRACKS.filter(t => t.status === 'downloaded')],
      
      // Active Downloads Queue
      downloadQueue: [],
      
      // Simulation Knobs
      isOnline: true, // Offline mode simulation toggle
      showEmptyLibrary: false, // For inspecting empty library state
      
      // UI Modals / Sheets
      isNowPlayingOpen: false,
      activeBottomSheet: null // 'track-options' | 'create-playlist' | 'queue' | 'confirm-clear'
    };

    this.listeners = new Set();
  }

  getState() {
    return this.state;
  }

  setState(partial) {
    this.state = { ...this.state, ...partial };
    this.notify();
  }

  subscribe(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  notify() {
    for (const listener of this.listeners) {
      listener(this.state);
    }
  }

  // Helper selectors
  getDownloadedTracks() {
    if (this.state.showEmptyLibrary) return [];
    return this.state.tracks.filter(t => t.status === 'downloaded');
  }

  getDriveTracks(folderId = null) {
    if (folderId) {
      return this.state.tracks.filter(t => t.folderId === folderId);
    }
    return this.state.tracks;
  }

  getDownloadedAlbums() {
    if (this.state.showEmptyLibrary) return [];
    return this.state.albums.filter(album => {
      const albumTracks = this.state.tracks.filter(t => album.trackIds.includes(t.id));
      return albumTracks.some(t => t.status === 'downloaded');
    });
  }

  getOfflineStorageUsed() {
    if (this.state.showEmptyLibrary) return '0 MB';
    const downloaded = this.getDownloadedTracks();
    let totalMB = 0;
    downloaded.forEach(t => {
      const mb = parseFloat(t.size);
      if (!isNaN(mb)) totalMB += mb;
    });
    return totalMB > 1024 ? `${(totalMB / 1024).toFixed(1)} GB` : `${Math.round(totalMB)} MB`;
  }
}

export const appState = new StateStore();
