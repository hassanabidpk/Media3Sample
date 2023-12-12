package com.hassan.media3sample

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.generativelanguage.v1beta2.DiscussServiceClient
import com.google.ai.generativelanguage.v1beta2.DiscussServiceSettings
import com.google.ai.generativelanguage.v1beta2.Example
import com.google.ai.generativelanguage.v1beta2.GenerateMessageRequest
import com.google.ai.generativelanguage.v1beta2.GenerateTextRequest
import com.google.ai.generativelanguage.v1beta2.Message
import com.google.ai.generativelanguage.v1beta2.MessagePrompt
import com.google.ai.generativelanguage.v1beta2.TextPrompt
import com.google.ai.generativelanguage.v1beta2.TextServiceClient
import com.google.ai.generativelanguage.v1beta2.TextServiceSettings
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.FixedHeaderProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

class PalmViewModel : ViewModel() {

    companion object {
        val TAG = PalmViewModel::class.simpleName
    }

    val rotateParamLiveData = MutableLiveData<Float>()
    private val _messages = MutableStateFlow<List<Message>>(value = listOf())
    private val _output = MutableStateFlow(value = "")
    private val _floatData = MutableStateFlow<Float>(0f)
    private val _textOverlay = MutableStateFlow<String>("")
    private val _trimSecondsList = MutableStateFlow<List<Int>>(value = listOf())
    val output: StateFlow<String>
        get() = _output
    val floatdata: StateFlow<Float>
        get() = _floatData
    val textOverlay: StateFlow<String>
        get() = _textOverlay
    val trimSecondsList: StateFlow<List<Int>>
        get() = _trimSecondsList
    val aIcontext: String = "Context: We are creating a video editing app using Media3 transformer APIs on Android.\n" +
            "Users will write a prompt and this tool can reply with specific function. \n" +
            "programming language = Kotlin.\n" +
            "For example user can ask : Trim video from 2 seconds to 10 seconds mark. Then this tool should return function trimVideo(2, 10). the parameters in the functions are start and end times \n" +
            "\n" +
            "Some of other examples are \n" +
            "1. Rotate video by 90 degrees: Return function with rotateVideo(90f). The parameter in the function is float value \n" +
            "2. Apply ZoomIn Effect : Return function name with zoomInVideo \n" +
            "3. Remove audio : Return function name removeAudio\n" +
            "4. transcode video to h.264 : Return fuction name transcodeVideotoH264\n" +
            "5. Add Text Overlay with text = \"Hello DevFest KL\" and color = \"green\" : Return function applyTextOverLay(\"Hello DevFest KL\", \"green\")\n" +
            "5. Add Image or Bitmap Overlay with Dino image : Return function applyBitmapOverLay(\"dino_image\")\n" +
            "\n" +
            "notes : \n" +
            "1. Reply function name without colon\n" +
            "2. Stitching videos feature is not available\n" +
            "\n" +
            "Additional information on : https://developer.android.com/guide/topics/media/transformer/transformations#kotlin\n"
    val exampleQuestions : String = "Question: Add Text Overlay with text = \"Hello DevFest KL\" and color = green\n" +
            "Answer : applyTextOverlay(\"Hello DevFest KL\", \"green\")\n" +
            "Question : transcode Video to h.265\n" +
            "Answer: transcodeVideotoH265 \n" +
            "Question : Add image or Bitmap overlay with Dino image\n" +
            "Answer: applyBitmapOverlay(\"dino_image\")\n"

    val messages: StateFlow<List<Message>>
        get() = _messages

    private var client: TextServiceClient

    init {
        // Initialize the Text Service Client
        client = initializeTextServiceClient(
            apiKey = "YOUR_API_KEY_HERE"
        )

        // Create the message prompt
        val prompt = createPrompt("Write precise instructions to edit video")
        // Send the first request
        val request = createTextRequest(prompt)
        // temporarily do not call below in init
        /* generateText(request) */
    }

    fun sendPrompt(userInput: String) {
        val prompt = createPrompt(userInput)

        val request = createTextRequest(prompt)
        generateText(request)
    }

    private fun initializeTextServiceClient(
        apiKey: String
    ): TextServiceClient {
        // (This is a workaround because GAPIC java libraries don't yet support API key auth)
        val transportChannelProvider = InstantiatingGrpcChannelProvider.newBuilder()
            .setHeaderProvider(FixedHeaderProvider.create(hashMapOf("x-goog-api-key" to apiKey)))
            .build()

        // Create TextServiceSettings
        val settings = TextServiceSettings.newBuilder()
            .setTransportChannelProvider(transportChannelProvider)
            .setCredentialsProvider(FixedCredentialsProvider.create(null))
            .build()

        // Initialize a TextServiceClient
        val textServiceClient = TextServiceClient.create(settings)

        return textServiceClient
    }


    private fun createPrompt(
        textContent: String
    ): TextPrompt {
        val fullTextForPrompt = aIcontext + exampleQuestions + "Question:" + textContent + "\nAnswer:"
        val textPrompt = TextPrompt.newBuilder()
            .setText(fullTextForPrompt)
            .build()

        return textPrompt
    }


    private fun createTextRequest(prompt: TextPrompt): GenerateTextRequest {
        return GenerateTextRequest.newBuilder()
            .setModel("models/text-bison-001") // Required, which model to use to generate the result
            .setPrompt(prompt) // Required
            .setTemperature(0.5f) // Optional, controls the randomness of the output
            .setCandidateCount(1) // Optional, the number of generated messages to return
            .build()
    }

    private fun generateText(
        request: GenerateTextRequest
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = client.generateText(request)
                val returnedText = response.candidatesList.last()
                when {
                    returnedText.output.contains("rotateVideo", true) -> {
                        val parameter = extractParameter(returnedText.output)
                        val degreesInFloat = parameter?.toFloat()
                        _floatData.update {  degreesInFloat!! }
                        Log.d(TAG, "returnedMessage(rotateVideo): ${returnedText.output}} | parameter : " +
                                "$degreesInFloat of type ${degreesInFloat!!::class}")
                        /*  withContext(Dispatchers.Main) {
                                 rotateParamLiveData.value = degreesInFloat
                             }*/
                    }
                    returnedText.output.contains("applyTextOverlay", true)-> {
                        val parameter = extractParameterForOverlay(returnedText.output)
                        val extractedOverlayText = parameter?.toString()
                        Log.d(TAG, "returnedMessage(applyTextOverlay): ${returnedText.output}} | parameter : " +
                                "$extractedOverlayText of type ${extractedOverlayText!!::class}")
                        if(extractedOverlayText != null && extractedOverlayText.isNotBlank())
                            _textOverlay.update { extractedOverlayText!! }
                    }
                    returnedText.output.contains("applyBitmapOverlay", true)-> {
                        val parameter = extractParameterForBitmapOverlay(returnedText.output)
                        val extractedOverlayText = parameter?.toString()
                        Log.d(TAG, "returnedMessage(applyTextOverlay): ${returnedText.output}} | parameter : " +
                                "$extractedOverlayText of type ${extractedOverlayText!!::class}")
                        if(extractedOverlayText != null && extractedOverlayText.isNotBlank())
                            _textOverlay.update { extractedOverlayText!! }
                    }
                    returnedText.output.contains("trimVideo", true) -> {
                        val parameter = extractIntParamsFromFunction(returnedText.output)
                        val trimSecondsList = parameter
                        Log.d(TAG, "returnedMessage(trimVideo): ${returnedText.output}} | parameter : " +
                                "$trimSecondsList of type ${trimSecondsList!!::class}")
                        if(!trimSecondsList.isNullOrEmpty())
                            _trimSecondsList.update { trimSecondsList }
                    }
                    else -> {
                        Log.d(TAG, "returnedMessage : ${returnedText.output}} for other cases")
                    }
                }
                // display the returned text in the UI
                _output.update { returnedText.output }

            } catch (e: Exception) {
                // There was an error, let's add a new text with the details
                _output.update { "API Error: ${e.message}" }
            }
        }
    }

    fun extractParameter(input: String): String? {
        val pattern = "rotateVideo\\((.*?)\\)".toRegex()
        val matchResult = pattern.find(input)
        return matchResult?.groups?.get(1)?.value
    }
    fun extractParameterForOverlay(input: String): String? {
        val pattern = "applyTextOverlay\\((.*?)\\)".toRegex()
        val matchResult = pattern.find(input)
        return matchResult?.groups?.get(1)?.value
    }
    fun extractParameterForBitmapOverlay(input: String): String? {
        val pattern = "applyBitmapOverlay\\((.*?)\\)".toRegex()
        val matchResult = pattern.find(input)
        return matchResult?.groups?.get(1)?.value
    }

    fun extractIntParamsFromFunction(input: String): List<Int> {
        val regex = """trimVideo\((\d+),\s*(\d+)\)""".toRegex()
        val matchResult = regex.find(input)

        return matchResult?.groupValues?.drop(1)?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    fun resetState() {
        Log.d(TAG, "function parameter reset")
        _floatData.update { 0f }  // Reset to empty or initial state
    }
}