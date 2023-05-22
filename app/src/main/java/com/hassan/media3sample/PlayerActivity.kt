package com.hassan.media3sample

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.hassan.media3sample.databinding.ActivityPlayerBinding

class PlayerActivity: AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }


    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer(){
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
//                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()

            }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}