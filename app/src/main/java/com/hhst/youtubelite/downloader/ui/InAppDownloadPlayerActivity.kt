package com.hhst.youtubelite.downloader.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hhst.youtubelite.R

/** Plays a completed download without handing it to another installed app. */
class InAppDownloadPlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uri = intent.getParcelableExtra<Uri>(EXTRA_URI) ?: run {
            finish()
            return
        }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val playerView = PlayerView(this).apply {
            useController = true
            controllerAutoShow = true
            controllerHideOnTouch = true
            controllerShowTimeoutMs = CONTROLLER_TIMEOUT_MS
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        setContentView(playerView)
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            exoPlayer.setMediaItem(
                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder().setTitle(title.ifBlank { getString(R.string.download) }).build(),
                    )
                    .build(),
            )
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onStop() {
        player?.release()
        player = null
        super.onStop()
    }

    companion object {
        private const val EXTRA_URI = "download_uri"
        private const val EXTRA_TITLE = "download_title"
        private const val CONTROLLER_TIMEOUT_MS = 3_000

        fun intent(context: Context, uri: Uri, title: String = ""): Intent =
            Intent(context, InAppDownloadPlayerActivity::class.java)
                .putExtra(EXTRA_URI, uri)
                .putExtra(EXTRA_TITLE, title)
    }
}
