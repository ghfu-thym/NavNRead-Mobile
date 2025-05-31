package com.example.navnreadmobile.ui.screen


import RssViewModel
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.platform.LocalViewConfiguration
import com.example.navnreadmobile.utils.CommandProcessor
import com.example.navnreadmobile.utils.Modes
import com.example.navnreadmobile.utils.SpeechManager
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VoiceCommandScreen(
    navController: NavController,
    viewModel: RssViewModel
) {
    val mode = remember { mutableStateOf(Modes.NEWEST_NEWS) }
    val news by viewModel.rssItems.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recordedText by remember { mutableStateOf("") }
    var speechRecognizer: SpeechRecognizer? = remember { null }
    val viewConfiguration = LocalViewConfiguration.current
    val speechManager = remember { SpeechManager(context) }
    val commandProcessor = remember { CommandProcessor(context) }

    // Mở mic
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechManager.startListening(
                onPartialResult = { partialText ->
                    recordedText = partialText
                },
                onFinalResult = { result ->
                    recordedText = result
                    isListening = false

                    // Xử lý lệnh chuyển chế độ
                    val feedbackMessage = commandProcessor.changeMode(recordedText, mode = mode)

                    speechManager.speakText(feedbackMessage)

                    Log.d(mode.value.toString(), "Mode changed to: ${mode.value}")
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechManager.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()

                    val longPressTimeOut = viewConfiguration.longPressTimeoutMillis

                    try {
                        withTimeout(longPressTimeOut) {
                            do {
                                val event = awaitPointerEvent()
                                val anyUp = event.changes.any { it.id == down.id && !it.pressed }
                                if (anyUp) {
                                    break
                                }
                            } while (event.changes.any { it.id == down.id })
                        }
                    } catch (e: PointerEventTimeoutCancellationException) {
                        isListening = true
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.id == down.id && !it.pressed }) {
                                // Finger lifted - stop listening immediately
                                isListening = false
                                speechManager.stopListening()
                                break
                            }
                        } while (true)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nhấn và giữ để nói",
                style = MaterialTheme.typography.headlineSmall
            )
            if (recordedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Lời nói thu được: $recordedText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box{
                Text(
                    text = "Chế độ hiện tại: ${
                    when (mode.value) {
                        Modes.NEWEST_NEWS -> "Tin mới nhất"
                        Modes.SEARCH -> "Tìm kiếm"
                        Modes.CATEGORY -> "Chủ đề"
                    }}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Khi đang lắng nghe
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .align(Alignment.Center),
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Listening",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
            }
        }
    }
}
