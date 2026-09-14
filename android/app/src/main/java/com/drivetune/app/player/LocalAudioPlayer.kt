package com.drivetune.app.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileInputStream

class LocalAudioPlayer(private val context: Context) {

    private val TAG = "LocalAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPath: String? = null

    var onCompletionListener: (() -> Unit)? = null
    var onErrorListener: ((String) -> Unit)? = null

    val isPlaying: Boolean
        get() = try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }

    val currentPositionSeconds: Int
        get() = try {
            (mediaPlayer?.currentPosition ?: 0) / 1000
        } catch (e: Exception) {
            0
        }

    val durationSeconds: Int
        get() = try {
            (mediaPlayer?.duration ?: 0) / 1000
        } catch (e: Exception) {
            0
        }

    @Synchronized
    fun playFile(
        filePath: String,
        onCompletion: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): Boolean {
        this.onCompletionListener = onCompletion
        this.onErrorListener = onError

        val file = File(filePath)
        if (!file.exists() || !file.canRead() || file.length() <= 0) {
            Log.e(TAG, "Audio file does not exist or cannot be read: $filePath")
            onError?.invoke("Local audio file is missing or unreadable")
            return false
        }

        try {
            stopAndRelease()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Use FileInputStream to ensure direct private file descriptor access
                FileInputStream(file).use { fis ->
                    setDataSource(fis.fd)
                }

                setOnPreparedListener { mp ->
                    mp.start()
                    Log.d(TAG, "Playback started for $filePath, duration=${mp.duration}ms")
                }

                setOnCompletionListener {
                    Log.d(TAG, "Playback completed for $filePath")
                    onCompletionListener?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    onErrorListener?.invoke("Audio playback error ($what, $extra)")
                    true
                }

                prepareAsync()
            }

            mediaPlayer = player
            currentPlayingPath = filePath
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize playback for $filePath", e)
            onError?.invoke("Failed to play: ${e.localizedMessage ?: "Unknown error"}")
            stopAndRelease()
            return false
        }
    }

    @Synchronized
    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }

    @Synchronized
    fun resume(): Boolean {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
        }
        return false
    }

    @Synchronized
    fun seekTo(seconds: Int) {
        try {
            val millis = seconds * 1000
            mediaPlayer?.seekTo(millis)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking playback", e)
        }
    }

    @Synchronized
    fun stopAndRelease() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            currentPlayingPath = null
        }
    }
}
