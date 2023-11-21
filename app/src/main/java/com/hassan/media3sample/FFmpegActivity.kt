package com.hassan.media3sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.hassan.media3sample.databinding.ActivityFfmpegBinding
import java.io.File
import java.io.IOException


class FFmpegActivity : AppCompatActivity() {

    private var outputFile: File? = null
    private var player: Player? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = playbackStateListener()

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityFfmpegBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_ffmpeg)
        setContentView(viewBinding.root)
        playOutPutMediaItem(MediaItem.fromUri("file:///storage/emulated/0/Android/data/com.hassan.media3sample/cache/ffmpeg-output-596789853.mp4"))
        viewBinding.selectTranscodingButton.setOnClickListener{
            transcodeVideoAsync("Hello DevFest 2023 SG Attendees")

        }
        viewBinding.mediaInfoButton.setOnClickListener{
            getMediaInfo()
        }
    }

    fun transcodeVideo(){
        val session = FFmpegKit.execute("-i file1.mp4 -c:v mpeg4 file2.mp4")
        if (ReturnCode.isSuccess(session.returnCode)) {

            // SUCCESS
        } else if (ReturnCode.isCancel(session.returnCode)) {

            // CANCEL
        } else {

            // FAILURE
            Log.d(
                "FFmpegActivity",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.state,
                    session.returnCode,
                    session.failStackTrace
                )
            )
        }
    }

    private fun transcodeVideoAsync(text: String) {
        val filePath = "storage/emulated/0/DCIM/Camera/PXL_20231105_062030941.TS.mp4"
        val filePathOnline = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4"

        try {
            outputFile =
                createExternalFile("ffmpeg-output-" + Clock.DEFAULT.elapsedRealtime() + ".mp4")
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        val outputFilePath: String = outputFile!!.getAbsolutePath()
        Log.d("FFmpegActivity", "output complete path file://$outputFilePath: output path : $outputFilePath")


        FFmpegKit.executeAsync("-y -i $filePath -vf \"scale=960:540,drawtext=fontfile=/system/fonts/Roboto-Regular.ttf:text='$text':fontcolor=green:fontsize=48:x=(w-text_w)/2:y=(h-text_h)/2\"  -c:v mpeg4 $outputFilePath",
            { session ->
                val state = session.state
                val returnCode = session.returnCode

                // CALLED WHEN SESSION IS EXECUTED
                Log.d(
                    "FFmpegActivity",
                    String.format(
                        "FFmpeg process exited with state %s and rc %s.%s",
                        state,
                        returnCode,
                        session.failStackTrace
                    )
                )
                if(returnCode.isValueSuccess) {
                    runOnUiThread{
                        playOutPutMediaItem(
                            MediaItem.fromUri("file://$outputFilePath"))
                    }
                }
            }, {
                // CALLED WHEN SESSION PRINTS LOGS
                Log.d(
                    "FFmpegActivity","transcoding log : ${it.message}")
            }) {
            // CALLED WHEN SESSION GENERATES STATISTICS
            Log.d(
                "FFmpegActivity","transcoding output : ${it.toString()}")
        }
    }

    fun getMediaInfo(){

        val filePath = "storage/emulated/0/DCIM/Camera/PXL_20231105_062030941.TS.mp4"

        val mediaInformation = FFprobeKit
            .getMediaInformation(filePath)
        mediaInformation.mediaInformation
        Log.d("FFmpegActivity", "mediainfo : ${mediaInformation.mediaInformation.allProperties}")
    }

    @Throws(IOException::class)
    private fun createExternalFile(fileName: String): File {
        val file = File(externalCacheDir, fileName)
        check(!(file.exists() && !file.delete())) { "Could not delete the previous export output file" }
        check(file.createNewFile()) { "Could not create the export output file" }
        return file
    }

    private fun releasePlayer(){
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun playOutPutMediaItem(outputMediaItem: MediaItem) {
        viewBinding.ffmpegOutputPlayerView.setPlayer(null)
        releasePlayer()
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.ffmpegOutputPlayerView.player = exoPlayer
                viewBinding.ffmpegOutputPlayerView.controllerAutoShow = false
                exoPlayer.setMediaItem(outputMediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()

            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

}
private fun playbackStateListener() = object : Player.Listener {
    @SuppressLint("UnsafeOptInUsageError")
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        androidx.media3.common.util.Log.d(PlayerActivity.TAG, "changed state to $stateString")
    }
}
