package com.hassan.media3sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hassan.media3sample.ui.theme.Media3SampleTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Media3SampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        Greeting("Android", modifier = Modifier.padding(8.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        LaunchTransform()
                        Spacer(modifier = Modifier.height(16.dp))
                        LaunchFFmpeg()
                        Spacer(modifier = Modifier.height(16.dp))
                        LaunchPlayer()
                    }

                }
            }
        }
    }

    fun launchTransformActivity() {
        val intent = Intent(this, TransformerActivity::class.java)
        startActivity(intent)

    }

    fun launchFFMpegActivity() {
        val intent = Intent(this, FFmpegActivity::class.java)
        startActivity(intent)

    }
    fun launchPlayerActivity() {
        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)

    }

        @Composable
        fun Greeting(name: String, modifier: Modifier = Modifier) {
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }

        @Composable
        fun LaunchTransform() {
            Column(modifier = Modifier.padding(2.dp)) {
                val button = Button(onClick = {
                    launchTransformActivity()
                }) {
                    Text("Launch Transformer Activity")
                }
            }
        }

    @Composable
    fun LaunchPlayer() {
        Column(modifier = Modifier.padding(2.dp)) {
            val button = Button(onClick = {
                launchPlayerActivity()
            }) {
                Text("Launch ExoPlayer Activity")
            }
        }
    }

        @Composable
        fun LaunchFFmpeg() {
            Column(modifier = Modifier.padding(2.dp)) {
                val button = Button(onClick = {
                    launchFFMpegActivity()
                }) {
                    Text("Launch FFMPEG Activity")
                }
            }
        }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        Media3SampleTheme {
            Greeting("DevFest 2023 Singapore attendees")
            LaunchTransform()
            LaunchFFmpeg()
        }
    }

}

