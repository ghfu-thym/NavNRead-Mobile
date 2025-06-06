package com.example.navnreadmobile.utils

import RssViewModel
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.network.Summarize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Thêm sealed class để chỉ định loại phản hồi
sealed class ResponseType {
    // Phản hồi đã được đọc bởi CommandProcessor, không cần đọc lại
    object AlreadySpoken : ResponseType()

    // Phản hồi là lỗi, cần được đọc bởi VoiceCommandScreen
    object Error : ResponseType()

    // Phản hồi thông thường, cần được đọc bởi VoiceCommandScreen
    object Normal : ResponseType()
}

class CommandProcessor(private val context: Context) {
    private val summarizer = Summarize()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val speechManager = SpeechManager(context)

    // Kiểm tra xem có đang xử lý đọc tin nào không
    private var isProcessingReadNews = false

    // Thay đổi kiểu trả về của hàm processCommand
    fun processCommand(
        command: String,
        mode: MutableState<Modes>,
        newsIndex: MutableIntState,
        searchIndex: MutableIntState,
        newsList: List<RssItem>,
        searchResults: List<RssItem>,
        rssViewModel: RssViewModel
    ): Pair<String, ResponseType> {
        // Chuyển đổi lệnh về chữ thường để so sánh
        val lowercaseCommand = command.lowercase()

        // Xử lý lệnh chuyển chế độ
        if (lowercaseCommand.contains("chuyển sang")) {
            when {
                lowercaseCommand.contains("tin mới nhất") -> {
                    mode.value = Modes.NEWEST_NEWS
                    return Pair("Đã chuyển sang chế độ tin mới nhất", ResponseType.Normal)
                }

                lowercaseCommand.contains("tìm kiếm") -> {
                    mode.value = Modes.SEARCH
                    return Pair("Đã chuyển sang chế độ tìm kiếm", ResponseType.Normal)
                }

                lowercaseCommand.contains("chủ đề") -> {
                    mode.value = Modes.CATEGORY
                    return Pair("Đã chuyển sang chế độ đọc tin theo chủ đề", ResponseType.Normal)
                }

                else -> return Pair("Không nhận diện được chế độ yêu cầu", ResponseType.Error)
            }
        }
        // Xử lý lệnh đọc tin
        else if (lowercaseCommand.contains("đọc tin")) {
            // Nếu đang xử lý đọc tin thì bỏ qua yêu cầu mới
            if (isProcessingReadNews) {
                return Pair("Đang xử lý yêu cầu đọc tin, vui lòng đợi", ResponseType.Normal)
            }
            return processReadNewsCommand(
                mode.value,
                newsList,
                newsIndex,
                searchResults,
                searchIndex,
                rssViewModel
            )
        } else if(lowercaseCommand.contains("dừng đọc tin") ) {
            // Nếu đang xử lý đọc tin thì dừng lại
            if (isProcessingReadNews) {
                isProcessingReadNews = false
                speechManager.speakText("Đã dừng đọc tin")
                return Pair("Đã dừng đọc tin", ResponseType.Normal)
            } else {
                return Pair("Không có tin nào đang được đọc", ResponseType.Normal)
            }

        }
        // Xử lý lệnh chuyển tin
        else if (lowercaseCommand.contains("tin tiếp theo") || lowercaseCommand.contains("tin kế tiếp")) {
            // Reset lại trạng thái xử lý đọc tin
            isProcessingReadNews = false
            return processNextNewsCommand(
                mode.value,
                newsList,
                newsIndex,
                searchResults,
                searchIndex
            )
        } else if (lowercaseCommand.contains("tin trước") || lowercaseCommand.contains("tin trước đó")) {
            // Reset lại trạng thái xử lý đọc tin
            isProcessingReadNews = false
            return processPreviousNewsCommand(
                mode.value,
                newsList,
                newsIndex,
                searchResults,
                searchIndex
            )
        } else if (mode.value == Modes.SEARCH) {
            isProcessingReadNews = false
            return processSearchCommand(lowercaseCommand, rssViewModel, searchResults, searchIndex)
        }

        return Pair("Không nhận diện được lệnh", ResponseType.Normal)
    }

    private fun processSearchCommand(
        query: String,
        rssViewModel: RssViewModel,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState
    ): Pair<String, ResponseType> {

        scope.launch {
            try {
                speechManager.speakText("Đang tìm kiếm: $query")
                rssViewModel.loadSearchResults(query)

                delay(2000)

                val result = rssViewModel.searchResults.value
                if(result.isNotEmpty()){
                    val title = result[0].title
                    speechManager.speakText("Kết quả tìm kiếm: $title")
                } else {
                    speechManager.speakText("Không tìm thấy kết quả nào cho từ khóa: $query")
                }
            } catch (e: Exception) {
                Log.e("CommandProcessor", "Error loading search results: ${e.message}")
                speechManager.speakText("Không thể tìm kiếm tin tức. Vui lòng thử lại sau.")
            }
        }


        return Pair("Đang tìm kiếm: $query", ResponseType.AlreadySpoken)

    }

    private fun processReadNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState,
        rssViewModel: RssViewModel
    ): Pair<String, ResponseType> {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty() || newsIndex.intValue >= newsList.size) {
                    return Pair("Không có tin tức để đọc", ResponseType.Error)
                }

                val currentNews = newsList[newsIndex.intValue]
                val link = currentNews.link
                Log.d("CommandProcessor", "Link: $link")

                // Đánh dấu đang xử lý đọc tin
                isProcessingReadNews = true

                // Chỉ trả về thông báo, không đọc
                val initialResponse = "Đang tải và tóm tắt tin: ${currentNews.title}"

                // Đọc thông báo ban đầu (sẽ không đọc lại ở VoiceCommandScreen)
                speechManager.speakText(initialResponse)

                // Sử dụng coroutine để xử lý tác vụ bất đồng bộ
                scope.launch {
                    try {
                        // Tải nội dung bài báo
                        rssViewModel.loadFullArticle(link)

                        // Đợi nội dung được tải với timeout
                        var articleContent = ""
                        var attempts = 0
                        val maxAttempts = 5

                        while (attempts < maxAttempts) {
                            attempts++
                            delay(1000) // Đợi 1 giây mỗi lần kiểm tra

                            articleContent = rssViewModel.articleContent.value

                            if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                                break // Đã tải thành công
                            }

                            Log.d(
                                "CommandProcessor",
                                "Đang đợi nội dung bài báo, lần thử $attempts"
                            )
                        }

                        if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                            // Tóm tắt nội dung bằng AI
                            try {
                                // Đọc thông báo đang tải
                                //speechManager.speakText("Đang tóm tắt bài viết")

                                val summary = withContext(Dispatchers.IO) {
                                    summarizer.summarize(articleContent)
                                }
                                Log.d("CommandProcessor", "Summary: $summary")

                                // Lưu tóm tắt vào ViewModel để hiển thị
                                rssViewModel.updateSummary(summary)

                                // Đọc tóm tắt
                                speechManager.speakText(summary)
                            } catch (e: Exception) {
                                // Nếu có lỗi khi tóm tắt, thông báo và không đọc toàn bộ nội dung
                                Log.e("CommandProcessor", "Error summarizing article: ${e.message}")
                                speechManager.speakText("Không thể tóm tắt bài viết.")
                                rssViewModel.updateSummary("Không thể tóm tắt bài viết.")
                            }
                        } else {
                            speechManager.speakText("Không thể tải nội dung bài báo")
                            rssViewModel.updateSummary("Không thể tải nội dung bài báo")
                        }
                    } finally {
                        // Đánh dấu đã hoàn thành xử lý
                        isProcessingReadNews = false
                    }
                }

                return Pair(initialResponse, ResponseType.AlreadySpoken)
            }

            Modes.SEARCH -> {
                if (searchResults.isEmpty() || searchIndex.intValue >= searchResults.size) {
                    return Pair("Không có kết quả tìm kiếm để đọc", ResponseType.Error)
                }

                // Đánh dấu đang xử lý đọc tin
                isProcessingReadNews = true

                val currentSearch = searchResults[searchIndex.intValue]
                val link = currentSearch.link

                // Chỉ trả về thông báo, không đọc
                val initialResponse = "Đang tải và tóm tắt tin: ${currentSearch.title}"

                // Đọc thông báo ban đầu (sẽ không đọc lại ở VoiceCommandScreen)
                speechManager.speakText(initialResponse)

                // Sử dụng coroutine để xử lý tác vụ bất đồng bộ
                scope.launch {
                    try {
                        // Tải nội dung bài báo
                        rssViewModel.loadFullArticle(link)

                        // Đợi nội dung được tải với timeout
                        var articleContent = ""
                        var attempts = 0
                        val maxAttempts = 5

                        while (attempts < maxAttempts) {
                            attempts++
                            delay(1000) // Đợi 1 giây mỗi lần kiểm tra

                            articleContent = rssViewModel.articleContent.value

                            if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                                break // Đã tải thành công
                            }

                            Log.d(
                                "CommandProcessor",
                                "Đang đợi nội dung bài báo, lần thử $attempts"
                            )
                        }

                        if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                            // Tóm tắt nội dung bằng AI
                            try {
                                // Đọc thông báo đang tải
                                //speechManager.speakText("Đang tóm tắt bài viết")

                                val summary = withContext(Dispatchers.IO) {
                                    summarizer.summarize(articleContent)
                                }

                                // Lưu tóm tắt vào ViewModel để hiển thị
                                rssViewModel.updateSummary(summary)

                                // Đọc tóm tắt
                                speechManager.speakText(summary)
                            } catch (e: Exception) {
                                // Nếu có lỗi khi tóm tắt, thông báo và không đọc toàn bộ nội dung
                                Log.e("CommandProcessor", "Error summarizing article: ${e.message}")
                                speechManager.speakText("Không thể tóm tắt bài viết.")
                                rssViewModel.updateSummary("Không thể tóm tắt bài viết.")
                            }
                        } else {
                            speechManager.speakText("Không thể tải nội dung bài báo")
                            rssViewModel.updateSummary("Không thể tải nội dung bài báo")
                        }
                    } finally {
                        // Đánh dấu đã hoàn thành xử lý
                        isProcessingReadNews = false
                    }
                }

                return Pair(initialResponse, ResponseType.AlreadySpoken)
            }

            else -> return Pair(
                "Chức năng đọc tin trong chế độ này chưa được hỗ trợ",
                ResponseType.Error
            )
        }
    }

    private fun processNextNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState
    ): Pair<String, ResponseType> {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty()) {
                    return Pair("Không có tin tức nào", ResponseType.Error)
                }

                // Tăng chỉ số và đảm bảo không vượt quá giới hạn
                if (newsIndex.intValue < newsList.size - 1) {
                    newsIndex.intValue++
                } else {
                    return Pair("Đã đến tin cuối cùng", ResponseType.Error)
                }

                val currentNews = newsList[newsIndex.intValue]
                val response = "Tin tiếp theo: ${currentNews.title}"
                speechManager.speakText(response)
                return Pair(response, ResponseType.AlreadySpoken)
            }

            Modes.SEARCH -> {
                if (searchResults.isEmpty()) {
                    return Pair("Không có kết quả tìm kiếm nào", ResponseType.Error)
                }

                if (searchIndex.intValue < searchResults.size - 1) {
                    searchIndex.intValue++
                } else {
                    return Pair("Đã đến kết quả tìm kiếm cuối cùng", ResponseType.Error)
                }

                val currentResult = searchResults[searchIndex.intValue]
                val response = "Kết quả tìm kiếm tiếp theo: ${currentResult.title}"
                speechManager.speakText(response)
                return Pair(response, ResponseType.AlreadySpoken)
            }

            else -> return Pair(
                "Chức năng chuyển tin trong chế độ này chưa được hỗ trợ",
                ResponseType.Error
            )
        }
    }

    private fun processPreviousNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState
    ): Pair<String, ResponseType> {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty()) {
                    return Pair("Không có tin tức nào", ResponseType.Error)
                }

                // Giảm chỉ số và đảm bảo không âm
                if (newsIndex.intValue > 0) {
                    newsIndex.intValue--
                } else {
                    return Pair("Đã đến tin đầu tiên", ResponseType.Error)
                }

                val currentNews = newsList[newsIndex.intValue]
                val response = "Tin trước đó: ${currentNews.title}"
                speechManager.speakText(response)
                return Pair(response, ResponseType.AlreadySpoken)
            }

            Modes.SEARCH -> {
                if (searchResults.isEmpty()) {
                    return Pair("Không có kết quả tìm kiếm nào", ResponseType.Error)
                }

                if (searchIndex.intValue > 0) {
                    searchIndex.intValue--
                } else {
                    return Pair("Đã đến kết quả tìm kiếm đầu tiên", ResponseType.Error)
                }

                val currentResult = searchResults[searchIndex.intValue]
                val response = "Kết quả tìm kiếm trước đó: ${currentResult.title}"
                speechManager.speakText(response)
                return Pair(response, ResponseType.AlreadySpoken)
            }

            else -> return Pair(
                "Chức năng quay lại tin trước trong chế độ này chưa được hỗ trợ",
                ResponseType.Error
            )
        }
    }
}
