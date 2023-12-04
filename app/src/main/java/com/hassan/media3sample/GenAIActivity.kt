package com.hassan.media3sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.generativelanguage.v1beta2.Message
import com.hassan.media3sample.ui.theme.LLMSampleTheme

class GenAIActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LLMSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleUi(
                        onAction = { degrees -> rotateVideo(degrees) }
                    )
                }
            }
        }
    }

    fun rotateVideo(degrees: Float){
        Log.d(TAG,"rotateVideo called $degrees")
    }
}

val GenAIActivity_TAG = GenAIActivity::class.simpleName
@Composable
fun SampleUi(
    mainViewModel: MainViewModel = viewModel(),
    onAction: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val (inputText, setInputText) = remember { mutableStateOf("") }
/*    val messages: List<Message> by mainViewModel.messages.collectAsState()*/
    val floatValue: Float by mainViewModel.floatdata.collectAsState()
    val textOutput: String by mainViewModel.output.collectAsState()
    Log.d(GenAIActivity_TAG, "function parameter received : $floatValue and textOutPut : $textOutput")
    if(floatValue>0){
        onAction(floatValue)
    }
/*    mainViewModel.rotateParamLiveData.observe(context.applicationContext, Observer { data ->
        // Call the method with the data
        Log.d(TAG, "function parameter received : $data")
    })*/
    Column(
        modifier = Modifier.padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = inputText,
            onValueChange = setInputText,
            label = { Text("Input:") }
        )
        Button(
            onClick = {
                /*mainViewModel.sendMessage(inputText)*/
                      mainViewModel.sendPrompt(inputText)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Generate Text")
        }
        Card(
            modifier = Modifier.padding(vertical = 2.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = textOutput,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
/*        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages) { message ->
                Card(modifier = Modifier.padding(vertical = 2.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = message.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }*/
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    LLMSampleTheme {
        SampleUi()
    }
}