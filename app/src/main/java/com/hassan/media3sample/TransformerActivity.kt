package com.hassan.media3sample

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.ContactsContract.Directory
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Util
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Transformer.ProgressState
import com.google.common.collect.ImmutableList
import com.hassan.media3sample.databinding.ActivityTransformerBinding
import com.hassan.media3sample.ui.theme.LLMSampleTheme
import java.io.File
import java.io.IOException
import kotlin.math.min


@SuppressLint("UnsafeOptInUsageError")
class TransformerActivity : AppCompatActivity() {

    companion object {
        val TAG = TransformerActivity::class.simpleName
    }
    val FILE_PERMISSION_REQUEST_CODE = 1
    private var onPermissionsGranted: Runnable? = null
    private var selectLocalFileButton: Button? = null
    private var videoLocalFilePickerLauncher: ActivityResultLauncher<Intent>? = null
    private var localFileUri: Uri? = null
    private var outputFile: File? = null
    private val oldOutputFile: File? = null
    private var outputFilePath: String? = null

    private var player: Player? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val playbackStateListener: Player.Listener = playbackStateListener()

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityTransformerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_transformer)
        setContentView(viewBinding.root)

        viewBinding.composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LLMSampleTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PaLMUi(
                            onRotateAction = { degrees -> rotateVideo(degrees) } ,
                            onApplyTextOverlay = { degrees -> applyTextOverlayEffect(degrees, Color.GREEN) },
                            onTrimVideoAction = { trimList -> trimVideo((trimList.get(0)*1000).toLong(), (trimList.get(1)*1000).toLong())}
                        )
                    }
                }
            }
        }

        videoLocalFilePickerLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (result != null) {
                this.videoLocalFilePickerLauncherResult(
                    result
                )
            }
        }

        try {
            outputFile =
                createExternalCacheFile("transformer-output-" + Clock.DEFAULT.elapsedRealtime() + ".mp4")
//                createExternalDirectoryFile("transformer-output-" + Clock.DEFAULT.elapsedRealtime() + ".mp4")
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        outputFilePath= outputFile!!.getAbsolutePath()
        Log.d(TAG, "outputFilePath : $outputFilePath")
        viewBinding.selectLocalFileButton.setOnClickListener {
            selectLocalFile(
                it!!,
                Assertions.checkNotNull<ActivityResultLauncher<Intent>>(
                    videoLocalFilePickerLauncher
                ),  /* mimeType= */
                "video/*"
            )
        }

        viewBinding.selectTranscodingButton.setOnClickListener{

//            applyTextOverlayEffect(1f, "DevFest Singapore 2023", Color.GREEN)
            callEditFunctionByName( "rotateVideo", 90f)

        }

        val myObject = MyClass()
        callFunctionByName(myObject, "myFunction")

    }

    @SuppressLint("UnsafeOptInUsageError")
    fun startTranscodeTransformer(outputFilePath: String) {
        val inputMediaItem = MediaItem.fromUri(localFileUri!!)

        TransformationRequest.Builder()
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .build()
        val editedMediaItem = EditedMediaItem.Builder(inputMediaItem).setRemoveAudio(true).build()
        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputFilePath)
        updateProgress(transformer)

    }

    val transformerListener: Transformer.Listener =
       object : Transformer.Listener {

            override fun onCompleted(composition: Composition, result: ExportResult) {
                Log.d(TAG, "Transformer output file path : \"file://$outputFile\")")
                playOutPutMediaItem(
                    MediaItem.fromUri("file://$outputFile"))
                    Toast.makeText(
                        applicationContext, "Success ${result.averageVideoBitrate} with effects : ${composition.effects}",
                        Toast.LENGTH_LONG
                    )
                        .show()

            }

            override fun onError(composition: Composition, result: ExportResult,
                                 exception: ExportException) {
//                displayError(exception)
                Log.d(TAG, "Transformer exception :${exception.toString()}")
            }
        }

    fun updateProgress(transformer: Transformer) {
        val progressHolder = ProgressHolder()
        val mainHandler = Handler(mainLooper)
        mainHandler.post(
            object : Runnable {
                override fun run() {
                    val progressState: @ProgressState Int = transformer.getProgress(progressHolder)
                    updateProgressInUi(progressState, progressHolder)
                    if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                        mainHandler.postDelayed(/* r= */this,  /* delayMillis= */50)
                    }
                }
            }
        )

    }

    fun updateProgressInUi(progressState: Int, progressHolder: ProgressHolder) {
        val progressBar =  viewBinding.determinateProgressBar
        progressBar.visibility = View.VISIBLE
        progressBar.progress = progressState

    }


    fun rotateVideo (degree: Float) {
        Log.d(TAG, "called rotateVideo with $degree rotation")
        if(localFileUri == null){
            Toast.makeText(
                applicationContext, "Select file first",
                Toast.LENGTH_LONG
            )
                .show()

            return
        }
        val inputMediaItem = MediaItem.fromUri(localFileUri!!)
        val editedMediaItemWithRotation = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(ScaleAndRotateTransformation.Builder().setRotationDegrees(degree).build(),
                    Presentation.createForHeight(720))
            )).build()

        val transformer = Transformer.Builder(this)
            .addListener(transformerListener)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .build()
        transformer.start(editedMediaItemWithRotation, outputFilePath!!)
        updateProgress(transformer)

    }

    fun applyOutputResolutionVideo(height: Int) {
        val inputMediaItem = MediaItem.fromUri(localFileUri!!)
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
        transformer.start(editedMediaItemWithScale, outputFilePath!!)
    }

    fun trimVideo(start: Long, end: Long) {
        val inputMediaItem = MediaItem.Builder()
            .setUri(localFileUri!!)
            .setClippingConfiguration(
                ClippingConfiguration.Builder()
                    .setStartPositionMs(start) // from 10 seconds
                    .setEndPositionMs(end) // to 20 seconds
                    .build())
            .build()
        val editedMediaItemWithScale = EditedMediaItem.Builder(inputMediaItem)
            .build()

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithScale, outputFilePath!!)
        updateProgress(transformer)
    }

    fun applyZoomOutEffect(duration: Int) {
        val inputMediaItem = MediaItem.fromUri(localFileUri!!)
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
        val inputMediaItem = MediaItem.fromUri(localFileUri!!)
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

    fun applyTextOverlayEffect(overlayText: String, color: Int) {
        val alpha: Float = 1f
        if(localFileUri == null){
            Toast.makeText(
                applicationContext, "Select file first",
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        if(outputFilePath == null)
        {
            Toast.makeText(
                applicationContext, "Output file path is null",
                Toast.LENGTH_LONG
            )
                .show()

            return
        }

        val inputMediaItem = MediaItem.fromUri(localFileUri!!)
        val overlaysBuilder = ImmutableList.Builder<TextureOverlay>()
        val overlaySettings = OverlaySettings.Builder()
            .setAlphaScale(alpha) ///* defaultValue= * 1f
            .build()
        val overlayText = SpannableString(
            Assertions.checkNotNull<String>(overlayText)
        )
        overlayText.setSpan(
            ForegroundColorSpan(color),
            /* start= */ 0,
            overlayText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        overlaysBuilder.add(TextOverlay.createStaticTextOverlay(overlayText, overlaySettings))
        val editedMediaItemWithOverLays = EditedMediaItem.Builder(inputMediaItem)
            .setEffects(Effects(
                /* audioProcessors= */ listOf(),
                /* videoEffects= */ listOf(OverlayEffect(overlaysBuilder.build()))
            )).build()

        val transformer = Transformer.Builder(this)
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItemWithOverLays, outputFilePath!!)
    }

    private fun selectLocalFile(
        view: View, localFilePickerLauncher: ActivityResultLauncher<Intent>, mimeType: String
    ) {
        val permission =
            if (Util.SDK_INT >= 33) permission.READ_MEDIA_VIDEO else permission.READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission( /* context= */this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionsGranted =
                Runnable { launchLocalFilePicker(localFilePickerLauncher, mimeType) }
            ActivityCompat.requestPermissions( /* activity= */
                this,
                arrayOf<String>(permission), FILE_PERMISSION_REQUEST_CODE)
        } else {
            launchLocalFilePicker(localFilePickerLauncher, mimeType)
        }
    }

    private fun launchLocalFilePicker(
        localFilePickerLauncher: ActivityResultLauncher<Intent>, mimeType: String
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType(mimeType)
        Assertions.checkNotNull(localFilePickerLauncher).launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == FILE_PERMISSION_REQUEST_CODE && grantResults.size == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Assertions.checkNotNull<Runnable>(onPermissionsGranted).run()
        } else {
            Toast.makeText(
                applicationContext, getString(R.string.permission_denied),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    fun getRealPathFromURI(contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = this.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(columnIndex ?: 0)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
    private fun videoLocalFilePickerLauncherResult(result: ActivityResult) {
        val data = result.data
        if (data != null) {
            localFileUri = Assertions.checkNotNull<Uri>(data.data)
            viewBinding.selectedFileTextView.text = localFileUri.toString()

            Log.d("TransformerActivity", localFileUri.toString())
            val realFilePath = getRealPathFromURI(localFileUri!!)
            val myFile = File(localFileUri?.path.toString())
            val filePath = myFile.absolutePath
            Log.d("TransformerActivity", "file path : $filePath realfilePath: $realFilePath")
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.local_file_picker_failed),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    /** Creates a cache file, resetting it if it already exists.  */
    @Throws(IOException::class)
    private fun createExternalCacheFile(fileName: String): File {
        val file = File(externalCacheDir, fileName)
        check(!(file.exists() && !file.delete())) { "Could not delete the previous export output file" }
        check(file.createNewFile()) { "Could not create the export output file" }
        return file
    }

    @Throws(IOException::class)
    private fun createExternalDirectoryFile(fileName: String): File {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDirectory, fileName)
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
        releasePlayer()
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.outputPlayerView.player = exoPlayer
                viewBinding.outputPlayerView.controllerAutoShow = false
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

    class MyClass {
        fun myFunction() {
            println("Function called")
            Log.d(TAG, "Function called")
        }
    }
    fun callFunctionByName(obj: Any, functionName: String) {
        Log.d(TAG, "callFunctionByName $obj")
        val method = obj::class.members.find { it.name == functionName }
        method?.call(obj)
    }

    fun callEditFunctionByName(functionName: String, vararg args: Any) {
        Log.d(TAG, "callFunctionByName $functionName")
        val method = this::class.members.find { it.name == functionName }
//        method?.call(this)
        method?.let {
            if (it.parameters.size == args.size + 1) { // +1 for the instance parameter
                it.call(this, *args)
            } else {
                Log.e("Reflection", "Method with provided arguments not found.")
            }
        }
    }

    private fun playbackStateListener() = object : Player.Listener {
        @SuppressLint("UnsafeOptInUsageError")
        override fun onPlaybackStateChanged(playbackState: Int) {
            var stateString: String

            when(playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    stateString = "ExoPlayer.STATE_IDLE      -"
                }
                ExoPlayer.STATE_BUFFERING -> {
                    stateString = "ExoPlayer.STATE_BUFFERING -"
                }
                ExoPlayer.STATE_READY -> {
                    stateString = "ExoPlayer.STATE_READY     -"
                    viewBinding.determinateProgressBar.visibility = View.GONE
                }
                ExoPlayer.STATE_ENDED -> {
                    stateString = "ExoPlayer.STATE_ENDED     -"
                }
                else -> {
                    stateString= "UNKNOWN_STATE             -"
                }
            }
            androidx.media3.common.util.Log.d(PlayerActivity.TAG, "changed state to $stateString")
        }
    }
}


val TAG = TransformerActivity::class.simpleName
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PaLMUi(
    palmViewModel: PalmViewModel = viewModel(),
    onRotateAction: (Float) -> Unit = {},
    onApplyTextOverlay: (String) -> Unit = {},
    onTrimVideoAction: (List<Int>) -> Unit = {}
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }
    var floatValue = palmViewModel.floatdata.collectAsState()
    val overlayText = palmViewModel.textOverlay.collectAsState()
    val textOutput: String by palmViewModel.output.collectAsState()
    val trimVideoList = palmViewModel.trimSecondsList.collectAsState()
    Log.d(TAG, "function parameter received : $floatValue and textOutPut : $textOutput")
    if(floatValue.value > 0){
        onRotateAction(floatValue.value)
        palmViewModel.resetState()
    } else if(overlayText.value.isNotBlank()) {
        onApplyTextOverlay(overlayText.value)
        palmViewModel.resetState()
    } else if(!trimVideoList.value.isNullOrEmpty()) {
        onTrimVideoAction(trimVideoList.value)
        palmViewModel.resetState()
    }
    /*    palmViewModel.rotateParamLiveData.observe(context.applicationContext, Observer { data ->
            // Call the method with the data
            Log.d(TAG, "function parameter received : $data")
        })*/

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(mediumPadding),
//        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {

        Column(
            modifier = Modifier.padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = inputText,
                onValueChange = { inputText = it },
                label = { androidx.compose.material3.Text("Ask AI:") }
            )
            Button(
                onClick = {
                    /*mainViewModel.sendMessage(inputText)*/
                    if(inputText.isNotBlank()) {
                        palmViewModel.sendPrompt(inputText)
                    }
                    focusManager.clearFocus()
                    inputText = ""
                },
                modifier = Modifier.padding(8.dp)
            ) {
                androidx.compose.material3.Text("Apply Transformation")
            }
            Card(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    androidx.compose.material3.Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = textOutput,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleOutlinedTextFieldSample() {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Label") }
    )
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPaLMPreview() {
    LLMSampleTheme {
        PaLMUi()
    }
}
