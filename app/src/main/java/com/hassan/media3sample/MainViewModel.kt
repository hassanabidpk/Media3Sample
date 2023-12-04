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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

class MainViewModel : ViewModel() {

    companion object {
        val TAG = MainViewModel::class.simpleName
    }

    val rotateParamLiveData = MutableLiveData<Float>()
    private val _messages = MutableStateFlow<List<Message>>(value = listOf())
    private val _output = MutableStateFlow(value = "")
    private val _floatData = MutableStateFlow<Float>(0f)
    val output: StateFlow<String>
        get() = _output
    val floatdata: StateFlow<Float>
        get() = _floatData
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
            "\n" +
            "notes : \n" +
            "1. Reply function name without colon\n" +
            "2. Stitching videos feature is not available\n" +
            "\n" +
            "Additional information on : https://developer.android.com/guide/topics/media/transformer/transformations#kotlin\n"
    val exampleQuestions : String = "Question: Add Text Overlay with text = \"Hello DevFest KL\" and color = green\n" +
            "Answer : applyTextOverlay(\"Hello DevFest KL\", \"green\")\n" +
            "Question : transcode Video to h.265\n" +
            "Answer: transcodeVideotoH265 \n"

    val messages: StateFlow<List<Message>>
        get() = _messages

    private var clientChat: DiscussServiceClient
    private var client: TextServiceClient

    init {
        // Initialize the Discuss Service Client
        clientChat = initializeDiscussServiceClient(
            apiKey = "YOUR_API_KEY_HERE"
        )
        client = initializeTextServiceClient(
            apiKey = "YOUR_API_KEY_HERE"
        )

        // Create the message prompt
        val prompt = createPrompt("Write precise instructions to edit video")
//        val prompt = createPrompt("How tall is the Eiffel Tower?")

        // Send the first request to kickstart the conversation
/*        val request = createMessageRequest(prompt)
        generateMessage(request)*/

        // Send the first request
        val request = createTextRequest(prompt)
        // temporarily do not call below in init
        /* generateText(request) */
    }

    fun sendMessage(userInput: String) {
        val prompt = createPrompt(userInput)

/*        val request = createMessageRequest(prompt)
        generateMessage(request)*/
    }

    fun sendPrompt(userInput: String) {
        val prompt = createPrompt(userInput)

        val request = createTextRequest(prompt)
        generateText(request)
    }

    private fun initializeDiscussServiceClient(
        apiKey: String
    ): DiscussServiceClient {
        // (This is a workaround because GAPIC java libraries don't yet support API key auth)
        val transportChannelProvider = InstantiatingGrpcChannelProvider.newBuilder()
            .setHeaderProvider(FixedHeaderProvider.create(hashMapOf("x-goog-api-key" to apiKey)))
            .build()

        // Create DiscussServiceSettings
        val settings = DiscussServiceSettings.newBuilder()
            .setTransportChannelProvider(transportChannelProvider)
            .setCredentialsProvider(FixedCredentialsProvider.create(null))
            .build()

        // Initialize a DiscussServiceClient
        val discussServiceClient = DiscussServiceClient.create(settings)

        return discussServiceClient
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

    private fun createExamples() : Iterable<out Example> {
        val examples = listOf<Example>()

        val example1 = Example.newBuilder()
            .setInput(Message.newBuilder().setContent("Export video").build())
            .setOutput(Message.newBuilder().setContent("exportVideo()").build())
            .build()

        val example2 = Example.newBuilder()
            .setInput(Message.newBuilder().setContent("Add Text Overlay with text = \"Hello DevFest KL\" and color = green").build())
            .setOutput(Message.newBuilder().setContent("applyTextOverlay(\"Hello DevFest KL\", \"green\")").build())
            .build()

        val example3 = Example.newBuilder()
            .setInput(Message.newBuilder().setContent("transcode Video to h.265").build())
            .setOutput(Message.newBuilder().setContent("transcodeVideotoH265").build())
            .build()

        val example4 = Example.newBuilder()
            .setInput(Message.newBuilder().setContent("Add Bitmap Overlay with selectedImage").build())
            .setOutput(Message.newBuilder().setContent("applyBitmapOverlay").build())
            .build()

        return listOf(example1, example2, example3, example4)
    }
    private fun createSingleExample(): Example {
        val input = Message.newBuilder()
            .setContent("What is the capital of California?")
            .build()

        val response = Message.newBuilder()
            .setContent("If the capital of California is what you seek, Sacramento is where you ought to peek.")
            .build()
        val example = Example.newBuilder()
            .setInput(input)
            .setOutput(response)
            .build()

        return example
    }


    private fun createChatPrompt(
        messageContent: String
    ): MessagePrompt {
        val palmMessage = Message.newBuilder()
            .setAuthor("0")
            .setContent(messageContent)
            .build()

        // Add the new Message to the UI
        _messages.update {
            it.toMutableList().apply {
                add(palmMessage)
            }
        }

        val messagePrompt = MessagePrompt.newBuilder()
            .addMessages(palmMessage) // required
            .setContext(aIcontext) // optional
            .addAllExamples(createExamples())
            .build()

        return messagePrompt
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

    private fun createMessageRequest(prompt: MessagePrompt): GenerateMessageRequest {
        return GenerateMessageRequest.newBuilder()
            .setModel("models/chat-bison-001") // Required, which model to use to generate the result
            .setPrompt(prompt) // Required
            .setTemperature(0.5f) // Optional, controls the randomness of the output
            .setCandidateCount(1) // Optional, the number of generated messages to return
            .build()
    }

    private fun createTextRequest(prompt: TextPrompt): GenerateTextRequest {
        return GenerateTextRequest.newBuilder()
            .setModel("models/text-bison-001") // Required, which model to use to generate the result
            .setPrompt(prompt) // Required
            .setTemperature(0.5f) // Optional, controls the randomness of the output
            .setCandidateCount(1) // Optional, the number of generated messages to return
            .build()
    }

    private fun generateMessage(
        request: GenerateMessageRequest
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = clientChat.generateMessage(request)
                val returnedText = response.candidatesList.last()
                val returnedMessage = response.candidatesList.last()
                if(returnedMessage.content.contains("rotateVideo", true)) {
                    val parameter = extractParameter(returnedMessage.content)
                    val degreesInFloat = parameter?.toFloat()
                    _floatData.update {  degreesInFloat!! }
                    Log.d(TAG, "returnedMessage with rotateVideo: ${returnedMessage.content} | parameter : " +
                            "$degreesInFloat of type ${degreesInFloat!!::class}")
/*                    withContext(Dispatchers.Main) {
                        rotateParamLiveData.value = degreesInFloat
                    }*/

                } else {
                    Log.d(
                        TAG,
                        "returnedMessage : $returnedMessage} with type ${returnedMessage::class}"
                    )
                }
//                _output.update { returnedMessage.output }
/*                // display the returned message in the UI
                _messages.update {
                    // Add the response to the list
                    it.toMutableList().apply {
                        add(returnedMessage)
                    }
                }*/
            } catch (e: Exception) {
                // There was an error, let's add a new message with the details
                _output.update { "API Error: ${e.message}" }
/*                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.apply {
                        add(
                            Message.newBuilder()
                                .setAuthor("API Error")
                                .setContent(e.message)
                                .build()
                        )
                    }
                }*/
            }
        }
    }

    private fun generateText(
        request: GenerateTextRequest
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = client.generateText(request)
                val returnedText = response.candidatesList.last()
                if(returnedText.output.contains("rotateVideo", true)) {
                    val parameter = extractParameter(returnedText.output)
                    val degreesInFloat = parameter?.toFloat()
                    _floatData.update {  degreesInFloat!! }
                    Log.d(TAG, "returnedMessage with rotateVideo: ${returnedText.output}} | parameter : " +
                            "$degreesInFloat of type ${degreesInFloat!!::class}")
                    /*  withContext(Dispatchers.Main) {
                             rotateParamLiveData.value = degreesInFloat
                         }*/

                } else {
                    Log.d(
                        TAG,
                        "returnedMessage : $returnedText} with type ${returnedText::class}"
                    )
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
}