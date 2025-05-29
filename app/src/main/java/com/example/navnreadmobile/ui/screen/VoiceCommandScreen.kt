package com.example.navnreadmobile.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Icon
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VoiceCommandScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var processingAudio by remember { mutableStateOf(false) }
    var recordedText by remember { mutableStateOf("") }
    var speechRecognizer: SpeechRecognizer? = remember { null }
    val viewConfiguration = LocalViewConfiguration.current

    // Mở mic
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechRecognizer = startListening(context) { result ->
                recordedText = result
                isListening = false
                // Handle the result, e.g., navigate to another screen or display the text
            }
        }

    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
//                detectTapGestures(
//                    onLongPress = {
//                        if (!isListening) {
//                            isListening = true
//                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
//                        }
//                    }
//                )
                awaitEachGesture {
                    val down = awaitFirstDown()

                    val longPressTimeOut = viewConfiguration.longPressTimeoutMillis

                    try{
                        withTimeout(longPressTimeOut){

                            do{
                                val event = awaitPointerEvent()
                                val anyUp = event.changes.any{it.id == down.id && !it.pressed}
                                if(anyUp){
                                    break
                                }
                            } while (event.changes.any { it.id==down.id })
                        }
                    } catch (e: PointerEventTimeoutCancellationException){
                        isListening = true
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.id == down.id && !it.pressed }) {
                                // Finger lifted - stop listening immediately
                                speechRecognizer?.stopListening()
                                isListening = false
                                processingAudio = true
                                break
                            }
                        } while (true)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Command Screen",
                style = MaterialTheme.typography.headlineSmall
            )
            if(recordedText.isNotEmpty()){
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recorded Text: $recordedText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        //Khi dang lang nghe
        if(isListening){
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .align(Alignment.Center)

            ){
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Listening",
                    modifier = Modifier
                        .size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }


}

private fun vibrate(context: Context){
    val vibrator = context.getSystemService((Context.VIBRATOR_SERVICE)) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}

private fun startListening(context: Context, onResult: (String) -> Unit): SpeechRecognizer {

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            vibrate(context)
        }

        override fun onBeginningOfSpeech() {
            Log.d("BeginningOfSpeech", "Speech has started")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d("RmsChanged", "RMS changed: $rmsdB")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d("BufferReceived", "Buffer received: ${buffer?.size ?: 0} bytes")
        }

        override fun onEndOfSpeech() {
            Log.d("EndOfSpeech", "Speech has ended")
        }

        override fun onError(error: Int) {
            onResult("...")
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            Log.d("PartialResults", "Partial results received")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d("Event", "Event type: $eventType")
        }

    })

    speechRecognizer.startListening(speechRecognizerIntent)
    return speechRecognizer
}