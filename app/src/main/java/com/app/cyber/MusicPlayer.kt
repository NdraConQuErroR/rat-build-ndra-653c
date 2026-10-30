package com.app.cyber

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object MusicPlayer {
    private var player: MediaPlayer? = null

    fun play(ctx: Context, url: String) {
        try {
            stop()
            player = MediaPlayer().apply {
                setDataSource(ctx, Uri.parse(url))
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {}
    }
    fun stop() {
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null
    }
}
