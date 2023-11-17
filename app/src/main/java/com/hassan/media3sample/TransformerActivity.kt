package com.hassan.media3sample

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Transformer.ProgressState
import kotlin.math.min

@SuppressLint("UnsafeOptInUsageError")
class TransformerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transformer)
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun startTranscodeTransformer() {
        val inputMediaItem = MediaItem.fromUri("path_to_input_file")

        val editedMediaItem = EditedMediaItem.Builder(inputMediaItem).setRemoveAudio(true).build()
        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, "path_to_output_file")
        updateProgress(transformer)

    }

    val transformerListener: Transformer.Listener =
       object : Transformer.Listener {

            override fun onCompleted(composition: Composition, result: ExportResult) {
//                playOutput()
            }

            override fun onError(composition: Composition, result: ExportResult,
                                 exception: ExportException) {
//                displayError(exception)
            }
        }

    fun updateProgress(transformer: Transformer) {
        val progressHolder = ProgressHolder()
        val mainHandler = Handler(mainLooper)
        mainHandler.post(
            object : Runnable {
                override fun run() {
                    val progressState: @ProgressState Int = transformer.getProgress(progressHolder)
//                    updateProgressInUi(progressState, progressHolder)
                    if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                        mainHandler.postDelayed(/* r= */this,  /* delayMillis= */500)
                    }
                }
            }
        )

    }


    fun rotateVideo (degree: Float) {
        val inputMediaItem = MediaItem.fromUri("path_to_input_file")
        val editedMediaItemWithRotation = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(ScaleAndRotateTransformation.Builder().setRotationDegrees(90f).build())
            )).build()

    }

    fun reScaleVideo(height: Int) {
        val inputMediaItem = MediaItem.fromUri("path_to_input_file")
        val editedMediaItemWithScale = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(Presentation.createForHeight(80))
            )).build()

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithScale, "path_to_output_file")
    }

    fun trimVideo(start: Long, end: Long) {
        val inputMediaItem = MediaItem.Builder()
            .setUri("path_to_uri")
            .setClippingConfiguration(
                ClippingConfiguration.Builder()
                    .setStartPositionMs(10_000) // from 10 seconds
                    .setEndPositionMs(20_000) // to 20 seconds
                    .build())
            .build()
        val editedMediaItemWithScale = EditedMediaItem.Builder(inputMediaItem)
            .build()

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithScale, "path_to_output_file")
    }

    fun applyZoomOutEffect(duration: Int) {
        val inputMediaItem = MediaItem.fromUri("path_to_input_file")
        val zoomOutEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val scale = 2 - min(1f, presentationTimeUs / 1_000_000f) // Video will zoom from 2x to 1x in the first second
            transformationMatrix.postScale(/* sx= */ scale, /* sy= */ scale)
            transformationMatrix // The calculated transformations will be //applied each frame in turn
        }

        val editedMediaItemWithScale = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(zoomOutEffect)
            )).build()

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithScale, "path_to_output_file")
    }

    fun applyZoomInEffect(duration: Int) {
        val inputMediaItem = MediaItem.fromUri("path_to_input_file")
        val zoominEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val scale =min(1f, presentationTimeUs / (C.MICROS_PER_SECOND * 2f)) // Video will zoom in the first second
            transformationMatrix.postScale(/* sx= */ scale, /* sy= */ scale)
            transformationMatrix // The calculated transformations will be //applied each frame in turn
        }

        val editedMediaItemWithScale = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(zoominEffect)
            )).build()

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithScale, "path_to_output_file")
    }


}