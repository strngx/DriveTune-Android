/**
 * DriveTune Main Application Controller
 * Connects reactive state, UI components, Web Audio playback, and simulated Drive sync
 */

import { appState } from './state.js';
import { audioEngine } from './audio-engine.js';
import { Icons, renderTrackRow, renderAlbumCard, renderPlaylistCard, renderFolderCard, showToast } from './components.js';

class DriveTuneApp {
  constructor() {
    this.audio = audioEngine;
    this.state = appState;
    this.downloadInterval = null;
  }

  init() {
    this.bindEvents();
    this.bindAudioEvents();
    this.state.subscribe(() => this.render());
    this.render();
  }

  bindAudioEvents() {
    this.audio.onTimeUpdate = (currentTime, duration) => {
      this.updatePlayerProgress(currentTime, duration);
    };

    this.audio.onStateChange = (audioState) => {
      this.updatePlaybackControls(audioState);
    };

    this.audio.onEnded = () => {
      this.playNextTrack();
    };
  }

  bindEvents() {
    // Navigation items
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        const screen = item.dataset.targetScreen;
        if (screen) {
          this.navigateTo(screen);
        }
      });
    });

    // Device Emulator Toggles
    const toggleOfflineBtn = document.getElementById('btn-toggle-offline');
    if (toggleOfflineBtn) {
      toggleOfflineBtn.addEventListener('click', () => {
        const isOnline = !this.state.getState().isOnline;
        this.state.setState({ isOnline });
        showToast(
          isOnline ? 'Online: Google Drive connected' : "Offline Mode: Playing from local device storage", 
          isOnline ? 'info' : 'warning'
        );
      });
    }

    const toggleFrameBtn = document.getElementById('btn-toggle-frame');
    if (toggleFrameBtn) {
      toggleFrameBtn.addEventListener('click', () => {
        const wrapper = document.querySelector('.app-viewport-wrapper');
        wrapper.classList.toggle('frameless');
      });
    }

    const toggleEmptyBtn = document.getElementById('btn-toggle-empty-state');
    if (toggleEmptyBtn) {
      toggleEmptyBtn.addEventListener('click', () => {
        const showEmpty = !this.state.getState().showEmptyLibrary;
        this.state.setState({ showEmptyLibrary: showEmpty });
        showToast(showEmpty ? 'Simulating empty offline library' : 'Restored offline library items', 'info');
      });
    }

    // Mini-Player tap to expand
    const miniPlayer = document.getElementById('mini-player');
    if (miniPlayer) {
      miniPlayer.addEventListener('click', (e) => {
        if (!e.target.closest('.mini-controls')) {
          this.openNowPlaying();
        }
      });

      document.getElementById('mini-btn-play').addEventListener('click', (e) => {
        e.stopPropagation();
        this.togglePlayPause();
      });

      document.getElementById('mini-btn-next').addEventListener('click', (e) => {
        e.stopPropagation();
        this.playNextTrack();
      });
    }

    // Full Now Playing Controls
    document.getElementById('np-close-btn').addEventListener('click', () => {
      this.closeNowPlaying();
    });

    document.getElementById('np-play-btn').addEventListener('click', () => {
      this.togglePlayPause();
    });

    document.getElementById('np-prev-btn').addEventListener('click', () => {
      this.playPrevTrack();
    });

    document.getElementById('np-next-btn').addEventListener('click', () => {
      this.playNextTrack();
    });

    document.getElementById('np-shuffle-btn').addEventListener('click', () => {
      const shuffle = !this.state.getState().shuffle;
      this.state.setState({ shuffle });
      showToast(shuffle ? 'Shuffle enabled' : 'Shuffle disabled', 'info');
    });

    document.getElementById('np-repeat-btn').addEventListener('click', () => {
      const modes = ['off', 'all', 'one'];
      const current = this.state.getState().repeat;
      const next = modes[(modes.indexOf(current) + 1) % modes.length];
      this.state.setState({ repeat: next });
      showToast(`Repeat mode: ${next.toUpperCase()}`, 'info');
    });

    // Scrub bar interaction
    const scrubContainer = document.getElementById('np-scrub-container');
    if (scrubContainer) {
      scrubContainer.addEventListener('click', (e) => {
        const rect = scrubContainer.getBoundingClientRect();
        const clickX = e.clientX - rect.left;
        const percent = Math.max(0, Math.min(1, clickX / rect.width));
        const duration = this.audio.duration || 200;
        this.audio.seek(duration * percent);
      });
    }

    // Auth screen Google Sign In
    const googleLoginBtn = document.getElementById('btn-google-signin');
    if (googleLoginBtn) {
      googleLoginBtn.addEventListener('click', () => {
        googleLoginBtn.innerHTML = `<span>Connecting to Google Drive...</span>`;
        setTimeout(() => {
          this.state.setState({ isAuthenticated: true, currentScreen: 'library' });
          showToast('Connected to Google Drive! Synced 18 audio files.', 'success');
        }, 800);
      });
    }

    // Delegated Global Click Handlers
    document.addEventListener('click', (e) => {
      // Track row click to play
      const trackRow = e.target.closest('.track-row');
      if (trackRow && !e.target.closest('.btn-track-menu') && !e.target.closest('.track-checkbox')) {
        const trackId = trackRow.dataset.trackId;
        this.handleTrackClick(trackId);
        return;
      }

      // Album card click
      const albumCard = e.target.closest('.album-card');
      if (albumCard && !e.target.closest('.album-card-play-btn')) {
        const albumId = albumCard.dataset.albumId;
        this.openAlbumDetail(albumId);
        return;
      }

      // Album play button
      const albumPlayBtn = e.target.closest('.album-card-play-btn');
      if (albumPlayBtn) {
        e.stopPropagation();
        const albumId = albumPlayBtn.dataset.playAlbum;
        this.playAlbum(albumId);
        return;
      }

      // Playlist card click
      const playlistCard = e.target.closest('.playlist-card');
      if (playlistCard) {
        const playlistId = playlistCard.dataset.playlistId;
        this.openPlaylistDetail(playlistId);
        return;
      }

      // Folder card click
      const folderCard = e.target.closest('.folder-card');
      if (folderCard) {
        const folderId = folderCard.dataset.folderId;
        this.openDriveFolder(folderId);
        return;
      }

      // Track more options menu
      const trackMenuBtn = e.target.closest('.btn-track-menu');
      if (trackMenuBtn) {
        e.stopPropagation();
        const trackId = trackMenuBtn.dataset.trackId;
        this.openTrackMenu(trackId);
        return;
      }

      // Modal overlay close
      if (e.target.classList.contains('modal-overlay')) {
        this.closeModals();
      }
    });

    // Multi-Select Track Checkbox Handling
    document.addEventListener('change', (e) => {
      if (e.target.classList.contains('track-checkbox')) {
        const trackId = e.target.dataset.trackId;
        const selected = new Set(this.state.getState().selectedTrackIds);
        if (e.target.checked) {
          selected.add(trackId);
        } else {
          selected.delete(trackId);
        }
        this.state.setState({ selectedTrackIds: selected });
      }
    });

    // Drive multi-download buttons
    const btnDownloadSelected = document.getElementById('btn-download-selected');
    if (btnDownloadSelected) {
      btnDownloadSelected.addEventListener('click', () => {
        const selected = Array.from(this.state.getState().selectedTrackIds);
        if (selected.length > 0) {
          this.startBatchDownload(selected);
        }
      });
    }

    const btnSelectAllDrive = document.getElementById('btn-select-all-drive');
    if (btnSelectAllDrive) {
      btnSelectAllDrive.addEventListener('click', () => {
        const currentFolderId = this.state.getState().activeDriveFolderId;
        const tracks = this.state.getDriveTracks(currentFolderId);
        const allIds = new Set(tracks.map(t => t.id));
        this.state.setState({ selectedTrackIds: allIds });
      });
    }

    const btnClearSelection = document.getElementById('btn-clear-selection');
    if (btnClearSelection) {
      btnClearSelection.addEventListener('click', () => {
        this.state.setState({ selectedTrackIds: new Set() });
      });
    }

    // Sync Now button
    const btnSyncDrive = document.getElementById('btn-sync-drive');
    if (btnSyncDrive) {
      btnSyncDrive.addEventListener('click', () => {
        showToast('Scanning Google Drive for audio files...', 'info');
        setTimeout(() => {
          showToast('Drive sync complete! All metadata up to date.', 'success');
        }, 1200);
      });
    }

    // Library Tabs
    document.querySelectorAll('.tab-btn').forEach(tab => {
      tab.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('is-active'));
        tab.classList.add('is-active');
        const target = tab.dataset.tabTarget;
        document.querySelectorAll('.library-tab-panel').forEach(panel => {
          panel.style.display = panel.id === `tab-panel-${target}` ? 'block' : 'none';
        });
      });
    });

    // Empty state CTA -> Browse Drive
    const browseDriveBtns = document.querySelectorAll('.btn-browse-drive');
    browseDriveBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        this.navigateTo('drive');
      });
    });

    // Create Playlist Modal Actions
    const btnNewPlaylist = document.getElementById('btn-new-playlist');
    if (btnNewPlaylist) {
      btnNewPlaylist.addEventListener('click', () => {
        this.openCreatePlaylistModal();
      });
    }

    // Search inputs
    const driveSearch = document.getElementById('drive-search-input');
    if (driveSearch) {
      driveSearch.addEventListener('input', (e) => {
        this.filterDriveTracks(e.target.value);
      });
    }

    const librarySearch = document.getElementById('library-search-input');
    if (librarySearch) {
      librarySearch.addEventListener('input', (e) => {
        this.filterLibraryTracks(e.target.value);
      });
    }
  }

  navigateTo(screenName) {
    this.state.setState({
      currentScreen: screenName,
      activeDriveFolderId: screenName === 'drive' ? this.state.getState().activeDriveFolderId : null
    });
  }

  openNowPlaying() {
    const modal = document.getElementById('now-playing-modal');
    modal.classList.add('is-open');
    this.state.setState({ isNowPlayingOpen: true });
  }

  closeNowPlaying() {
    const modal = document.getElementById('now-playing-modal');
    modal.classList.remove('is-open');
    this.state.setState({ isNowPlayingOpen: false });
  }

  handleTrackClick(trackId) {
    const track = this.state.getState().tracks.find(t => t.id === trackId);
    if (!track) return;

    if (track.status !== 'downloaded') {
      // Prompt user that track needs to be downloaded before playing
      this.promptDownloadBeforePlay(track);
      return;
    }

    // Play track offline
    this.audio.loadTrack(track, true);
    this.state.setState({
      currentTrack: track,
      isPlaying: true
    });
  }

  promptDownloadBeforePlay(track) {
    if (!this.state.getState().isOnline) {
      showToast('Cannot download while offline. Please connect to Wi-Fi/data.', 'warning');
      return;
    }

    this.showConfirmationSheet({
      title: 'Download Required for Offline Play',
      message: `"${track.title}" is currently in Google Drive (${track.size}). Download it now to play offline?`,
      confirmText: 'Download & Play',
      onConfirm: () => {
        this.startSingleDownload(track, true);
      }
    });
  }

  startSingleDownload(track, autoPlayOnComplete = false) {
    if (!this.state.getState().isOnline) {
      showToast('Cannot sync in offline mode.', 'warning');
      return;
    }

    // Update track state to downloading
    const tracks = this.state.getState().tracks.map(t => {
      if (t.id === track.id) {
        return { ...t, status: 'downloading', progress: 5 };
      }
      return t;
    });
    this.state.setState({ tracks });
    showToast(`Downloading "${track.title}" from Drive...`, 'info');

    // Simulate download chunk increments
    let progress = 5;
    const interval = setInterval(() => {
      progress += Math.floor(Math.random() * 20) + 15;
      if (progress >= 100) {
        clearInterval(interval);
        const updatedTracks = this.state.getState().tracks.map(t => {
          if (t.id === track.id) {
            return { ...t, status: 'downloaded', progress: 100 };
          }
          return t;
        });
        this.state.setState({ tracks: updatedTracks });
        showToast(`Downloaded "${track.title}"! Available offline.`, 'success');

        if (autoPlayOnComplete) {
          this.handleTrackClick(track.id);
        }
      } else {
        const updatedTracks = this.state.getState().tracks.map(t => {
          if (t.id === track.id) {
            return { ...t, progress };
          }
          return t;
        });
        this.state.setState({ tracks: updatedTracks });
      }
    }, 400);
  }

  startBatchDownload(trackIds) {
    if (!this.state.getState().isOnline) {
      showToast('Cannot download in offline mode.', 'warning');
      return;
    }

    showToast(`Queued ${trackIds.length} tracks for download from Drive`, 'info');
    
    // Set all to downloading
    let currentIdx = 0;
    const downloadNext = () => {
      if (currentIdx >= trackIds.length) {
        this.state.setState({ selectedTrackIds: new Set() });
        showToast(`All ${trackIds.length} tracks downloaded to offline storage!`, 'success');
        return;
      }

      const id = trackIds[currentIdx];
      const track = this.state.getState().tracks.find(t => t.id === id);
      if (track && track.status !== 'downloaded') {
        this.startSingleDownload(track, false);
      }
      currentIdx++;
      setTimeout(downloadNext, 600);
    };

    downloadNext();
  }

  togglePlayPause() {
    this.audio.togglePlay();
    this.state.setState({ isPlaying: this.audio.isPlaying });
  }

  playNextTrack() {
    const st = this.state.getState();
    const downloaded = this.state.getDownloadedTracks();
    if (downloaded.length === 0) return;

    let nextIndex = 0;
    if (st.shuffle) {
      nextIndex = Math.floor(Math.random() * downloaded.length);
    } else {
      const currentIdx = downloaded.findIndex(t => t.id === st.currentTrack?.id);
      nextIndex = (currentIdx + 1) % downloaded.length;
    }

    const nextTrack = downloaded[nextIndex];
    if (nextTrack) {
      this.audio.loadTrack(nextTrack, true);
      this.state.setState({ currentTrack: nextTrack, isPlaying: true });
    }
  }

  playPrevTrack() {
    const st = this.state.getState();
    const downloaded = this.state.getDownloadedTracks();
    if (downloaded.length === 0) return;

    const currentIdx = downloaded.findIndex(t => t.id === st.currentTrack?.id);
    const prevIndex = currentIdx <= 0 ? downloaded.length - 1 : currentIdx - 1;
    const prevTrack = downloaded[prevIndex];
    if (prevTrack) {
      this.audio.loadTrack(prevTrack, true);
      this.state.setState({ currentTrack: prevTrack, isPlaying: true });
    }
  }

  playAlbum(albumId) {
    const album = this.state.getState().albums.find(a => a.id === albumId);
    if (!album) return;
    const albumTracks = this.state.getState().tracks.filter(t => album.trackIds.includes(t.id));
    const downloadedTracks = albumTracks.filter(t => t.status === 'downloaded');

    if (downloadedTracks.length === 0) {
      this.promptDownloadAlbum(album);
      return;
    }

    this.audio.loadTrack(downloadedTracks[0], true);
    this.state.setState({ currentTrack: downloadedTracks[0], isPlaying: true });
    showToast(`Playing album "${album.title}"`, 'success');
  }

  promptDownloadAlbum(album) {
    const albumTracks = this.state.getState().tracks.filter(t => album.trackIds.includes(t.id));
    this.showConfirmationSheet({
      title: `Download Album "${album.title}"`,
      message: `Download all ${albumTracks.length} tracks from Drive for offline playback?`,
      confirmText: 'Download Album',
      onConfirm: () => {
        this.startBatchDownload(album.trackIds);
      }
    });
  }

  openAlbumDetail(albumId) {
    this.state.setState({
      activeAlbumId: albumId,
      currentScreen: 'album-detail'
    });
  }

  openPlaylistDetail(playlistId) {
    this.state.setState({
      activePlaylistId: playlistId,
      currentScreen: 'playlist-detail'
    });
  }

  openDriveFolder(folderId) {
    this.state.setState({
      activeDriveFolderId: folderId,
      currentScreen: 'drive'
    });
  }

  openTrackMenu(trackId) {
    const track = this.state.getState().tracks.find(t => t.id === trackId);
    if (!track) return;

    const sheet = document.getElementById('bottom-sheet-content');
    const isDownloaded = track.status === 'downloaded';

    sheet.innerHTML = `
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 20px;">
        <img src="${track.coverArt}" style="width: 52px; height: 52px; border-radius: var(--radius-sm); object-fit: cover;">
        <div>
          <div style="font-size: 15px; font-weight: 700; color: #FFF;">${track.title}</div>
          <div style="font-size: 13px; color: var(--text-secondary);">${track.artist} • ${track.format}</div>
        </div>
      </div>
      <div style="display: flex; flex-direction: column; gap: 8px;">
        ${isDownloaded ? `
          <button class="btn btn-secondary sheet-action-btn" id="sheet-btn-play-now" style="justify-content: flex-start; padding: 14px 16px;">
            ${Icons.play} Play Now (Offline)
          </button>
          <button class="btn btn-secondary sheet-action-btn" id="sheet-btn-add-playlist" style="justify-content: flex-start; padding: 14px 16px;">
            ${Icons.plus} Add to Playlist
          </button>
          <button class="btn btn-secondary sheet-action-btn" id="sheet-btn-remove-download" style="justify-content: flex-start; padding: 14px 16px; color: #F87171;">
            ${Icons.trash} Delete Local File (${track.size})
          </button>
        ` : `
          <button class="btn btn-primary sheet-action-btn" id="sheet-btn-download-track" style="justify-content: flex-start; padding: 14px 16px;">
            ${Icons.downloadCloud} Download from Drive (${track.size})
          </button>
        `}
      </div>
    `;

    document.getElementById('modal-overlay').classList.add('is-visible');
    document.getElementById('global-bottom-sheet').classList.add('is-visible');

    // Bind sheet buttons
    setTimeout(() => {
      const playBtn = document.getElementById('sheet-btn-play-now');
      if (playBtn) {
        playBtn.addEventListener('click', () => {
          this.closeModals();
          this.handleTrackClick(track.id);
        });
      }

      const downloadBtn = document.getElementById('sheet-btn-download-track');
      if (downloadBtn) {
        downloadBtn.addEventListener('click', () => {
          this.closeModals();
          this.startSingleDownload(track, true);
        });
      }

      const removeBtn = document.getElementById('sheet-btn-remove-download');
      if (removeBtn) {
        removeBtn.addEventListener('click', () => {
          this.closeModals();
          const updated = this.state.getState().tracks.map(t => {
            if (t.id === track.id) return { ...t, status: 'cloud', progress: 0 };
            return t;
          });
          this.state.setState({ tracks: updated });
          showToast(`Removed local copy of "${track.title}". File still in Drive.`, 'info');
        });
      }

      const addPlBtn = document.getElementById('sheet-btn-add-playlist');
      if (addPlBtn) {
        addPlBtn.addEventListener('click', () => {
          this.closeModals();
          this.openAddToPlaylistModal(track.id);
        });
      }
    }, 50);
  }

  openCreatePlaylistModal() {
    const sheet = document.getElementById('bottom-sheet-content');
    sheet.innerHTML = `
      <div style="font-size: 18px; font-weight: 800; color: #FFF; margin-bottom: 8px;">Create New Playlist</div>
      <div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 16px;">Playlists are stored locally on your device for offline listening.</div>
      <div style="margin-bottom: 16px;">
        <input type="text" id="new-playlist-title" placeholder="Playlist Name (e.g. Night Cruiser)" class="search-box search-input" style="padding: 12px 16px; width: 100%; border-radius: var(--radius-md);" autofocus>
      </div>
      <div style="display: flex; gap: 10px;">
        <button class="btn btn-secondary" style="flex: 1;" id="btn-cancel-playlist">Cancel</button>
        <button class="btn btn-primary" style="flex: 1;" id="btn-save-playlist">Create</button>
      </div>
    `;

    document.getElementById('modal-overlay').classList.add('is-visible');
    document.getElementById('global-bottom-sheet').classList.add('is-visible');

    setTimeout(() => {
      document.getElementById('btn-cancel-playlist')?.addEventListener('click', () => this.closeModals());
      document.getElementById('btn-save-playlist')?.addEventListener('click', () => {
        const input = document.getElementById('new-playlist-title');
        const title = input ? input.value.trim() : '';
        if (title) {
          const newPl = {
            id: `pl-${Date.now()}`,
            title,
            description: 'Custom offline playlist',
            trackIds: ['trk-101', 'trk-201'],
            covers: [
              'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80',
              'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&q=80'
            ]
          };
          this.state.setState({
            playlists: [newPl, ...this.state.getState().playlists]
          });
          this.closeModals();
          showToast(`Created offline playlist "${title}"`, 'success');
        }
      });
    }, 50);
  }

  showConfirmationSheet({ title, message, confirmText, onConfirm }) {
    const sheet = document.getElementById('bottom-sheet-content');
    sheet.innerHTML = `
      <div style="font-size: 18px; font-weight: 800; color: #FFF; margin-bottom: 8px;">${title}</div>
      <div style="font-size: 13px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 20px;">${message}</div>
      <div style="display: flex; gap: 10px;">
        <button class="btn btn-secondary" style="flex: 1;" id="btn-dialog-cancel">Cancel</button>
        <button class="btn btn-primary" style="flex: 1;" id="btn-dialog-confirm">${confirmText}</button>
      </div>
    `;

    document.getElementById('modal-overlay').classList.add('is-visible');
    document.getElementById('global-bottom-sheet').classList.add('is-visible');

    setTimeout(() => {
      document.getElementById('btn-dialog-cancel')?.addEventListener('click', () => this.closeModals());
      document.getElementById('btn-dialog-confirm')?.addEventListener('click', () => {
        this.closeModals();
        if (onConfirm) onConfirm();
      });
    }, 50);
  }

  closeModals() {
    document.getElementById('modal-overlay').classList.remove('is-visible');
    document.getElementById('global-bottom-sheet').classList.remove('is-visible');
  }

  updatePlayerProgress(currentTime, duration) {
    // Update Full Now Playing scrub bar
    const fill = document.getElementById('np-scrub-fill');
    const curLabel = document.getElementById('np-time-current');
    const remLabel = document.getElementById('np-time-remaining');
    const miniFill = document.getElementById('mini-progress-fill');

    const pct = duration > 0 ? (currentTime / duration) * 100 : 0;
    if (fill) fill.style.width = `${pct}%`;
    if (miniFill) miniFill.style.width = `${pct}%`;

    const formatTime = (secs) => {
      const m = Math.floor(secs / 60);
      const s = Math.floor(secs % 60);
      return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    if (curLabel) curLabel.textContent = formatTime(currentTime);
    if (remLabel) remLabel.textContent = `-${formatTime(Math.max(0, duration - currentTime))}`;
  }

  updatePlaybackControls(audioState) {
    const npPlayBtn = document.getElementById('np-play-btn');
    const miniPlayBtn = document.getElementById('mini-btn-play');

    const icon = audioState.isPlaying ? Icons.pause : Icons.play;
    if (npPlayBtn) npPlayBtn.innerHTML = icon;
    if (miniPlayBtn) miniPlayBtn.innerHTML = icon;
  }

  render() {
    const st = this.state.getState();

    // 1. Update Android Status Bar & Offline indicators
    const statusOfflineIcon = document.querySelector('.status-offline-indicator');
    if (statusOfflineIcon) {
      statusOfflineIcon.classList.toggle('is-offline', !st.isOnline);
    }
    const offlineBanners = document.querySelectorAll('.offline-banner');
    offlineBanners.forEach(b => b.classList.toggle('is-active', !st.isOnline));

    // Emulator button active state
    const emulatorOfflineBtn = document.getElementById('btn-toggle-offline');
    if (emulatorOfflineBtn) {
      emulatorOfflineBtn.classList.toggle('active', !st.isOnline);
      emulatorOfflineBtn.innerHTML = st.isOnline 
        ? `${Icons.offline} Sim: Offline` 
        : `${Icons.cloud} Sim: Online`;
    }

    // 2. Active Screen visibility
    document.querySelectorAll('.screen-container').forEach(screen => {
      screen.classList.remove('is-active');
    });

    const activeEl = document.getElementById(`screen-${st.currentScreen}`);
    if (activeEl) {
      activeEl.classList.add('is-active');
    }

    // 3. Bottom Navigation active state
    document.querySelectorAll('.nav-item').forEach(item => {
      const isCurrent = item.dataset.targetScreen === st.currentScreen ||
        (st.currentScreen === 'album-detail' && item.dataset.targetScreen === 'library') ||
        (st.currentScreen === 'playlist-detail' && item.dataset.targetScreen === 'playlists');
      item.classList.toggle('is-active', isCurrent);
    });

    // 4. Render Mini-Player content
    const miniPlayer = document.getElementById('mini-player');
    if (miniPlayer) {
      if (st.currentTrack) {
        miniPlayer.classList.remove('is-hidden');
        document.getElementById('mini-thumb').src = st.currentTrack.coverArt;
        document.getElementById('mini-title').textContent = st.currentTrack.title;
        document.getElementById('mini-artist-text').textContent = `${st.currentTrack.artist} • ${st.currentTrack.format}`;
      } else {
        miniPlayer.classList.add('is-hidden');
      }
    }

    // 5. Render Now Playing Modal metadata
    if (st.currentTrack) {
      document.getElementById('np-art-img').src = st.currentTrack.coverArt;
      document.getElementById('np-song-title').textContent = st.currentTrack.title;
      document.getElementById('np-artist-name').textContent = `${st.currentTrack.artist} — ${st.currentTrack.album}`;
      document.getElementById('np-album-subtitle').textContent = st.currentTrack.album;
      document.getElementById('np-offline-badge-text').textContent = `OFFLINE • ${st.currentTrack.format} ${st.currentTrack.bitrate}`;
    }

    // 6. Screen Specific Renderers
    if (st.currentScreen === 'library') {
      this.renderLibraryScreen();
    } else if (st.currentScreen === 'drive') {
      this.renderDriveScreen();
    } else if (st.currentScreen === 'playlists') {
      this.renderPlaylistsScreen();
    } else if (st.currentScreen === 'album-detail') {
      this.renderAlbumDetailScreen();
    } else if (st.currentScreen === 'playlist-detail') {
      this.renderPlaylistDetailScreen();
    } else if (st.currentScreen === 'sync-queue') {
      this.renderSyncQueueScreen();
    } else if (st.currentScreen === 'settings') {
      this.renderSettingsScreen();
    }
  }

  renderLibraryScreen() {
    const st = this.state.getState();
    const downloadedTracks = this.state.getDownloadedTracks();
    const downloadedAlbums = this.state.getDownloadedAlbums();

    // Storage badge
    const storagePill = document.getElementById('lib-storage-pill');
    if (storagePill) {
      storagePill.textContent = `${this.state.getOfflineStorageUsed()} Offline`;
    }

    // Empty state vs normal state
    const emptyStateEl = document.getElementById('lib-empty-state');
    const contentEl = document.getElementById('lib-main-content');

    if (downloadedTracks.length === 0) {
      if (emptyStateEl) emptyStateEl.style.display = 'flex';
      if (contentEl) contentEl.style.display = 'none';
      return;
    }

    if (emptyStateEl) emptyStateEl.style.display = 'none';
    if (contentEl) contentEl.style.display = 'block';

    // Featured Album
    const featuredAlbum = downloadedAlbums[0] || st.albums[0];
    const featCover = document.getElementById('lib-featured-bg');
    const featTitle = document.getElementById('lib-featured-title');
    const featArtist = document.getElementById('lib-featured-artist');
    if (featCover && featuredAlbum) featCover.src = featuredAlbum.coverArt;
    if (featTitle && featuredAlbum) featTitle.textContent = featuredAlbum.title;
    if (featArtist && featuredAlbum) featArtist.textContent = `${featuredAlbum.artist} • ${featuredAlbum.year}`;

    // Albums Tab
    const albumsGrid = document.getElementById('lib-albums-grid');
    if (albumsGrid) {
      albumsGrid.innerHTML = downloadedAlbums.map(album => {
        const count = st.tracks.filter(t => album.trackIds.includes(t.id) && t.status === 'downloaded').length;
        return renderAlbumCard(album, count, true);
      }).join('');
    }

    // Playlists Tab
    const playlistsGrid = document.getElementById('lib-playlists-grid');
    if (playlistsGrid) {
      playlistsGrid.innerHTML = st.playlists.map(pl => renderPlaylistCard(pl)).join('');
    }

    // All Tracks Tab
    const tracksList = document.getElementById('lib-tracks-list');
    if (tracksList) {
      tracksList.innerHTML = downloadedTracks.map(track => {
        return renderTrackRow(track, {
          isPlaying: st.isPlaying && st.currentTrack?.id === track.id,
          showCheckbox: false,
          showDuration: true,
          context: 'library'
        });
      }).join('');
    }
  }

  renderDriveScreen() {
    const st = this.state.getState();
    const folderId = st.activeDriveFolderId;
    const tracks = this.state.getDriveTracks(folderId);

    // Multi-select bar
    const selectBar = document.getElementById('multi-select-bar');
    const selectCount = document.getElementById('multi-select-count');
    const selectedCount = st.selectedTrackIds.size;
    if (selectBar) {
      selectBar.classList.toggle('is-active', selectedCount > 0);
    }
    if (selectCount) {
      selectCount.textContent = `${selectedCount} item${selectedCount > 1 ? 's' : ''} selected`;
    }

    // Folders Grid (Hide if inside folder)
    const folderSection = document.getElementById('drive-folders-section');
    const foldersGrid = document.getElementById('drive-folders-grid');
    const backToFoldersBtn = document.getElementById('btn-back-drive-folders');
    const folderTitle = document.getElementById('drive-current-folder-title');

    if (folderId) {
      const activeFolder = st.folders.find(f => f.id === folderId);
      if (folderSection) folderSection.style.display = 'none';
      if (backToFoldersBtn) backToFoldersBtn.style.display = 'flex';
      if (folderTitle) folderTitle.textContent = activeFolder ? activeFolder.name : 'Folder Files';
    } else {
      if (folderSection) folderSection.style.display = 'block';
      if (backToFoldersBtn) backToFoldersBtn.style.display = 'none';
      if (folderTitle) folderTitle.textContent = 'All Audio Files in Drive';
      if (foldersGrid) {
        foldersGrid.innerHTML = st.folders.map(f => renderFolderCard(f)).join('');
      }
    }

    // Back button in folder drilldown
    if (backToFoldersBtn) {
      backToFoldersBtn.onclick = () => {
        this.state.setState({ activeDriveFolderId: null });
      };
    }

    // Audio Files list in Drive
    const driveTrackList = document.getElementById('drive-tracks-list');
    if (driveTrackList) {
      driveTrackList.innerHTML = tracks.map(track => {
        return renderTrackRow(track, {
          isPlaying: st.isPlaying && st.currentTrack?.id === track.id,
          isSelected: st.selectedTrackIds.has(track.id),
          showCheckbox: true,
          showDuration: true,
          context: 'drive'
        });
      }).join('');
    }
  }

  renderPlaylistsScreen() {
    const st = this.state.getState();
    const grid = document.getElementById('playlists-screen-grid');
    if (grid) {
      grid.innerHTML = st.playlists.map(pl => renderPlaylistCard(pl)).join('');
    }
  }

  renderAlbumDetailScreen() {
    const st = this.state.getState();
    const album = st.albums.find(a => a.id === st.activeAlbumId) || st.albums[0];
    if (!album) return;

    const albumTracks = st.tracks.filter(t => album.trackIds.includes(t.id));
    const isAllDownloaded = albumTracks.every(t => t.status === 'downloaded');

    document.getElementById('album-detail-art').src = album.coverArt;
    document.getElementById('album-detail-title').textContent = album.title;
    document.getElementById('album-detail-artist').textContent = album.artist;
    document.getElementById('album-detail-source').textContent = `Source: ${album.folderPath}`;
    document.getElementById('album-detail-meta').textContent = `${albumTracks.length} tracks • ${album.year} • Offline FLAC`;

    const dlBtn = document.getElementById('btn-album-download-all');
    if (dlBtn) {
      dlBtn.style.display = isAllDownloaded ? 'none' : 'inline-flex';
      dlBtn.onclick = () => this.startBatchDownload(album.trackIds);
    }

    const playBtn = document.getElementById('btn-album-play');
    if (playBtn) {
      playBtn.onclick = () => this.playAlbum(album.id);
    }

    const trackList = document.getElementById('album-detail-tracks');
    if (trackList) {
      trackList.innerHTML = albumTracks.map(track => {
        return renderTrackRow(track, {
          isPlaying: st.isPlaying && st.currentTrack?.id === track.id,
          showCheckbox: false,
          showDuration: true,
          context: 'album-detail'
        });
      }).join('');
    }
  }

  renderPlaylistDetailScreen() {
    const st = this.state.getState();
    const pl = st.playlists.find(p => p.id === st.activePlaylistId) || st.playlists[0];
    if (!pl) return;

    const plTracks = st.tracks.filter(t => pl.trackIds.includes(t.id));

    document.getElementById('pl-detail-title').textContent = pl.title;
    document.getElementById('pl-detail-meta').textContent = `${plTracks.length} tracks • Offline Playlist`;

    const playBtn = document.getElementById('btn-playlist-play');
    if (playBtn) {
      playBtn.onclick = () => {
        if (plTracks.length > 0) {
          this.handleTrackClick(plTracks[0].id);
        }
      };
    }

    const trackList = document.getElementById('playlist-detail-tracks');
    if (trackList) {
      trackList.innerHTML = plTracks.map(track => {
        return renderTrackRow(track, {
          isPlaying: st.isPlaying && st.currentTrack?.id === track.id,
          showCheckbox: false,
          showDuration: true,
          context: 'playlist-detail'
        });
      }).join('');
    }
  }

  renderSyncQueueScreen() {
    const st = this.state.getState();
    const downloadingTracks = st.tracks.filter(t => t.status === 'downloading');
    const downloadedTracks = this.state.getDownloadedTracks();

    document.getElementById('sync-available-count').textContent = st.tracks.length;
    document.getElementById('sync-downloaded-count').textContent = downloadedTracks.length;
    document.getElementById('sync-storage-used').textContent = this.state.getOfflineStorageUsed();

    const queueList = document.getElementById('sync-queue-list');
    if (queueList) {
      if (downloadingTracks.length === 0) {
        queueList.innerHTML = `
          <div style="text-align: center; padding: 24px; color: var(--text-tertiary); font-size: 13px;">
            No active downloads. All selected music is synced to offline storage.
          </div>
        `;
      } else {
        queueList.innerHTML = downloadingTracks.map(track => `
          <div class="queue-item">
            <div class="queue-item-header">
              <span style="font-size: 13px; font-weight: 700; color: #FFF;">${track.title}</span>
              <span style="font-size: 12px; font-weight: 700; color: var(--status-downloading);">${track.progress}%</span>
            </div>
            <div class="queue-progress-bar">
              <div class="queue-progress-fill" style="width: ${track.progress}%;"></div>
            </div>
            <div class="queue-item-meta">
              <span>Google Drive • ${track.size}</span>
              <span>1.8 MB/s</span>
            </div>
          </div>
        `).join('');
      }
    }
  }

  renderSettingsScreen() {
    const st = this.state.getState();
    document.getElementById('settings-user-name').textContent = st.user.name;
    document.getElementById('settings-user-email').textContent = st.user.email;
    document.getElementById('settings-offline-size').textContent = this.state.getOfflineStorageUsed();

    const btnClearStorage = document.getElementById('btn-clear-storage');
    if (btnClearStorage) {
      btnClearStorage.onclick = () => {
        this.showConfirmationSheet({
          title: 'Delete All Offline Music?',
          message: 'This will remove local music files from your device to free up storage. Your music remains safe in Google Drive.',
          confirmText: 'Delete Local Files',
          onConfirm: () => {
            const tracks = st.tracks.map(t => ({ ...t, status: 'cloud', progress: 0 }));
            this.state.setState({ tracks });
            showToast('Cleared all local music files.', 'info');
          }
        });
      };
    }
  }

  filterDriveTracks(query) {
    const q = query.toLowerCase();
    const rows = document.querySelectorAll('#drive-tracks-list .track-row');
    rows.forEach(row => {
      const title = row.querySelector('.track-title')?.textContent.toLowerCase() || '';
      const artist = row.querySelector('.track-artist')?.textContent.toLowerCase() || '';
      row.style.display = (title.includes(q) || artist.includes(q)) ? 'flex' : 'none';
    });
  }

  filterLibraryTracks(query) {
    const q = query.toLowerCase();
    const rows = document.querySelectorAll('#lib-tracks-list .track-row');
    rows.forEach(row => {
      const title = row.querySelector('.track-title')?.textContent.toLowerCase() || '';
      const artist = row.querySelector('.track-artist')?.textContent.toLowerCase() || '';
      row.style.display = (title.includes(q) || artist.includes(q)) ? 'flex' : 'none';
    });
  }
}

window.addEventListener('DOMContentLoaded', () => {
  const app = new DriveTuneApp();
  app.init();
  window.driveTune = app;
});
