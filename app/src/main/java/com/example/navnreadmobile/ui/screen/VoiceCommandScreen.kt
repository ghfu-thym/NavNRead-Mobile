package com.example.navnreadmobile.ui.screen


import RssViewModel
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.navnreadmobile.utils.CommandProcessor
import com.example.navnreadmobile.utils.Modes
import com.example.navnreadmobile.utils.SpeechManager

@Composable
fun VoiceCommandScreen(
    navController: NavController,
    viewModel: RssViewModel
) {
    val mode = remember { mutableStateOf(Modes.NEWEST_NEWS) }
    val news by viewModel.rssItems.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val newsIndex = remember { mutableIntStateOf(0) }
    val searchIndex = remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recordedText by remember { mutableStateOf("") }
    var speechRecognizer: SpeechRecognizer? = remember { null }
    val viewConfiguration = LocalViewConfiguration.current
    val speechManager = remember { SpeechManager(context) }
    val commandProcessor = remember { CommandProcessor(context) }
    val articleContent by viewModel.articleContent.collectAsState("")
    val articleSummary by viewModel.articleSummary.collectAsState("")
    val scrollState = rememberScrollState()

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
                    val feedbackMessage = commandProcessor.processCommand(recordedText, mode = mode, newsIndex,
                        searchIndex, news, searchResults, viewModel)

                    //speechManager.speakText(feedbackMessage)


                    Log.d(mode.value.toString(), "Mode changed to: ${mode.value}")
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if(news.isEmpty()){
            viewModel.loadNews("https://vnexpress.net/rss/tin-moi-nhat.rss", "Tin mới nhất")
            if (news.isNotEmpty()) {
                viewModel.loadFullArticle(news.getOrNull(0)?.link ?: "")
            }
        }
    }

    // Clear summary when changing news items
    LaunchedEffect(newsIndex.intValue, searchIndex.intValue, mode.value) {
        viewModel.clearSummary()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nhấn và giữ để nói",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (recordedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Lời nói thu được: $recordedText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp)
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Chế độ hiện tại: ${
                        when (mode.value) {
                            Modes.NEWEST_NEWS -> "Tin mới nhất"
                            Modes.SEARCH -> "Tìm kiếm"
                            Modes.CATEGORY -> "Chủ đề"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )

                // Hiển thị tiêu đề tin tức
                Text(
                    text = when (mode.value) {
                        Modes.NEWEST_NEWS -> news.getOrNull(newsIndex.intValue)?.title ?: "Chưa có tin tức"
                        Modes.SEARCH -> if (searchResults.isNotEmpty()) {
                            searchResults.getOrNull(searchIndex.intValue)?.title ?: "Chưa có tin tức"
                        } else {
                            "Chưa có tin tức"
                        }
                        else -> "Chưa có tin tức"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider()

                // Hiển thị số thứ tự và tổng số tin
                Text(
                    text = when (mode.value) {
                        Modes.NEWEST_NEWS -> "Tin ${newsIndex.intValue + 1}/${news.size}"
                        Modes.SEARCH -> "Kết quả ${searchIndex.intValue + 1}/${searchResults.size}"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                // Hiển thị nội dung tóm tắt hoặc hướng dẫn
                if (articleSummary.isNotEmpty()) {
                    Text(
                        text = articleSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "Sử dụng lệnh \"đọc tin\" để xem nội dung tóm tắt của bài báo này",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị hướng dẫn điều hướng
                Text(
                    text = "Lệnh hỗ trợ:\n• \"đọc tin\" - đọc nội dung tin hiện tại\n• \"tin tiếp theo\" - chuyển đến tin kế tiếp\n• \"tin trước\" - quay lại tin trước đó",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
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
