/**
 * DriveTune Audio Engine
 * Built on Web Audio API to produce rich audio feedback, synth melodic chords,
 * and realistic player time simulation for 100% offline playback experience.
 */

class AudioEngine {
  constructor() {
    this.ctx = null;
    this.masterGain = null;
    this.isPlaying = false;
    this.currentTrack = null;
    this.currentTime = 0;
    this.duration = 0;
    this.timer = null;
    this.oscNodes = [];
    this.onTimeUpdate = null;
    this.onEnded = null;
    this.onStateChange = null;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
        this.masterGain = this.ctx.createGain();
        this.masterGain.gain.setValueAtTime(0.3, this.ctx.currentTime);
        this.masterGain.connect(this.ctx.destination);
      }
    }
  }

  loadTrack(track, autoPlay = true) {
    this.init();
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    this.stopSynth();
    this.currentTrack = track;
    this.currentTime = 0;
    this.duration = track.duration;

    if (autoPlay) {
      this.play();
    } else {
      this.notifyState();
    }
  }

  play() {
    if (!this.currentTrack) return;
    this.init();
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    this.isPlaying = true;
    this.startSynth();
    this.startTimer();
    this.notifyState();
  }

  pause() {
    this.isPlaying = false;
    this.stopSynth();
    this.stopTimer();
    this.notifyState();
  }

  togglePlay() {
    if (this.isPlaying) {
      this.pause();
    } else {
      this.play();
    }
  }

  seek(timeInSeconds) {
    this.currentTime = Math.max(0, Math.min(this.duration, timeInSeconds));
    if (this.onTimeUpdate) {
      this.onTimeUpdate(this.currentTime, this.duration);
    }
  }

  startTimer() {
    this.stopTimer();
    this.timer = setInterval(() => {
      this.currentTime += 1;
      if (this.currentTime >= this.duration) {
        this.stopTimer();
        this.stopSynth();
        this.isPlaying = false;
        this.notifyState();
        if (this.onEnded) this.onEnded();
      } else {
        if (this.onTimeUpdate) {
          this.onTimeUpdate(this.currentTime, this.duration);
        }
      }
    }, 1000);
  }

  stopTimer() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  startSynth() {
    if (!this.ctx || !this.masterGain) return;
    this.stopSynth();

    const baseFreq = this.currentTrack ? (this.currentTrack.audioFreq || 440) : 440;
    
    // Create dual oscillator warm pad / synth chord
    const osc1 = this.ctx.createOscillator();
    const osc2 = this.ctx.createOscillator();
    const filter = this.ctx.createBiquadFilter();
    const subGain = this.ctx.createGain();

    osc1.type = 'triangle';
    osc1.frequency.setValueAtTime(baseFreq, this.ctx.currentTime);

    osc2.type = 'sine';
    osc2.frequency.setValueAtTime(baseFreq * 1.5, this.ctx.currentTime); // Perfect fifth

    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(1200, this.ctx.currentTime);

    subGain.gain.setValueAtTime(0.001, this.ctx.currentTime);
    subGain.gain.exponentialRampToValueAtTime(0.2, this.ctx.currentTime + 0.5);

    osc1.connect(filter);
    osc2.connect(filter);
    filter.connect(subGain);
    subGain.connect(this.masterGain);

    osc1.start();
    osc2.start();

    this.oscNodes = [osc1, osc2, subGain];
  }

  stopSynth() {
    if (this.oscNodes.length > 0) {
      try {
        const subGain = this.oscNodes[2];
        if (subGain && this.ctx) {
          subGain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.1);
        }
        setTimeout(() => {
          this.oscNodes.forEach(node => {
            if (node.stop) node.stop();
            if (node.disconnect) node.disconnect();
          });
          this.oscNodes = [];
        }, 120);
      } catch (e) {
        this.oscNodes = [];
      }
    }
  }

  setVolume(vol) { // 0.0 to 1.0
    if (this.masterGain && this.ctx) {
      this.masterGain.gain.setValueAtTime(Math.max(0, Math.min(1, vol * 0.5)), this.ctx.currentTime);
    }
  }

  notifyState() {
    if (this.onStateChange) {
      this.onStateChange({
        isPlaying: this.isPlaying,
        currentTrack: this.currentTrack,
        currentTime: this.currentTime,
        duration: this.duration
      });
    }
  }
}

export const audioEngine = new AudioEngine();
