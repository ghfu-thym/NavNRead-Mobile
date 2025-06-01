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

class CommandProcessor(private val context: Context) {
    private val summarizer = Summarize()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val speechManager = SpeechManager(context)

    // Kiểm tra xem có đang xử lý đọc tin nào không
    private var isProcessingReadNews = false

    fun processCommand(
        command: String,
        mode: MutableState<Modes>,
        newsIndex: MutableIntState,
        searchIndex: MutableIntState,
        newsList: List<RssItem>,
        searchResults: List<RssItem>,
        rssViewModel: RssViewModel
    ): String {
        // Convert to lowercase to make matching case-insensitive
        val lowercaseCommand = command.lowercase()

        // Xử lý lệnh chuyển chế độ
        if (lowercaseCommand.contains("chuyển sang")) {
            when {
                lowercaseCommand.contains("tin mới nhất") -> {
                    mode.value = Modes.NEWEST_NEWS
                    return "Đã chuyển sang chế độ tin mới nhất"
                }
                lowercaseCommand.contains("tìm kiếm") -> {
                    mode.value = Modes.SEARCH
                    return "Đã chuyển sang chế độ tìm kiếm"
                }
                lowercaseCommand.contains("chủ đề") -> {
                    mode.value = Modes.CATEGORY
                    return "Đã chuyển sang chế độ đọc tin theo chủ đề"
                }
                else -> return "Không nhận diện được chế độ yêu cầu"
            }
        }
        // Xử lý lệnh đọc tin
        else if (lowercaseCommand.contains("đọc tin")) {
            // Nếu đang xử lý đọc tin thì bỏ qua yêu cầu mới
            if (isProcessingReadNews) {
                return "Đang xử lý yêu cầu đọc tin, vui lòng đợi"
            }
            return processReadNewsCommand(mode.value, newsList, newsIndex, searchResults, searchIndex, rssViewModel)
        }
        // Xử lý lệnh chuyển tin
        else if (lowercaseCommand.contains("tin tiếp theo") || lowercaseCommand.contains("tin kế tiếp")) {
            // Reset lại trạng thái xử lý đọc tin
            isProcessingReadNews = false
            return processNextNewsCommand(mode.value, newsList, newsIndex, searchResults, searchIndex)
        } else if (lowercaseCommand.contains("tin trước") || lowercaseCommand.contains("tin trước đó")) {
            // Reset lại trạng thái xử lý đọc tin
            isProcessingReadNews = false
            return processPreviousNewsCommand(mode.value, newsList, newsIndex, searchResults, searchIndex)
        }

        return "Không nhận diện được lệnh"
    }

    private fun processReadNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState,
        rssViewModel: RssViewModel
    ): String {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty() || newsIndex.intValue >= newsList.size) {
                    return "Không có tin tức để đọc"
                }

                val currentNews = newsList[newsIndex.intValue]
                val link = currentNews.link
                Log.d("CommandProcessor", "Link: $link")

                // Đánh dấu đang xử lý đọc tin
                isProcessingReadNews = true

                // Chỉ trả về thông báo, không đọc
                val initialResponse = "Đang tải và tóm tắt tin: ${currentNews.title}"

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

                            Log.d("CommandProcessor", "Đang đợi nội dung bài báo, lần thử $attempts")
                        }

                        if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                            // Tóm tắt nội dung bằng AI
                            try {
                                // Đọc thông báo đang tải
                                speechManager.speakText("Đang tóm tắt bài viết")

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

                return initialResponse
            }
            Modes.SEARCH -> {
                // Xử lý đọc tin trong chế độ tìm kiếm
                if (searchResults.isEmpty() || searchIndex.intValue >= searchResults.size) {
                    return "Không có kết quả tìm kiếm để đọc"
                }

                // Đánh dấu đang xử lý đọc tin
                isProcessingReadNews = true

                val currentSearch = searchResults[searchIndex.intValue]
                val link = currentSearch.link

                // Chỉ trả về thông báo, không đọc
                val initialResponse = "Đang tải và tóm tắt tin: ${currentSearch.title}"

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

                            Log.d("CommandProcessor", "Đang đợi nội dung bài báo, lần thử $attempts")
                        }

                        if (articleContent.isNotEmpty() && !articleContent.startsWith("Lỗi tải bài báo")) {
                            // Tóm tắt nội dung bằng AI
                            try {
                                // Đọc thông báo đang tải
                                speechManager.speakText("Đang tóm tắt bài viết")

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

                return initialResponse
            }
            else -> return "Chức năng đọc tin trong chế độ này chưa được hỗ trợ"
        }
    }

    private fun processNextNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState
    ): String {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty()) {
                    return "Không có tin tức nào"
                }

                // Tăng chỉ số và đảm bảo không vượt quá giới hạn
                if (newsIndex.intValue < newsList.size - 1) {
                    newsIndex.intValue++
                } else {
                    return "Đã đến tin cuối cùng"
                }

                val currentNews = newsList[newsIndex.intValue]
                val response = "Tin tiếp theo: ${currentNews.title}"
                speechManager.speakText(response)
                return response
            }
            Modes.SEARCH -> {
                if (searchResults.isEmpty()) {
                    return "Không có kết quả tìm kiếm nào"
                }

                if (searchIndex.intValue < searchResults.size - 1) {
                    searchIndex.intValue++
                } else {
                    return "Đã đến kết quả tìm kiếm cuối cùng"
                }

                val currentResult = searchResults[searchIndex.intValue]
                val response = "Kết quả tìm kiếm tiếp theo: ${currentResult.title}"
                speechManager.speakText(response)
                return response
            }
            else -> return "Chức năng chuyển tin trong chế độ này chưa được hỗ trợ"
        }
    }

    private fun processPreviousNewsCommand(
        mode: Modes,
        newsList: List<RssItem>,
        newsIndex: MutableIntState,
        searchResults: List<RssItem>,
        searchIndex: MutableIntState
    ): String {
        when (mode) {
            Modes.NEWEST_NEWS -> {
                if (newsList.isEmpty()) {
                    return "Không có tin tức nào"
                }

                // Giảm chỉ số và đảm bảo không âm
                if (newsIndex.intValue > 0) {
                    newsIndex.intValue--
                } else {
                    return "Đã đến tin đầu tiên"
                }

                val currentNews = newsList[newsIndex.intValue]
                val response = "Tin trước đó: ${currentNews.title}"
                speechManager.speakText(response)
                return response
            }
            Modes.SEARCH -> {
                if (searchResults.isEmpty()) {
                    return "Không có kết quả tìm kiếm nào"
                }

                if (searchIndex.intValue > 0) {
                    searchIndex.intValue--
                } else {
                    return "Đã đến kết quả tìm kiếm đầu tiên"
                }

                val currentResult = searchResults[searchIndex.intValue]
                val response = "Kết quả tìm kiếm trước đó: ${currentResult.title}"
                speechManager.speakText(response)
                return response
            }
            else -> return "Chức năng quay lại tin trước trong chế độ này chưa được hỗ trợ"
        }
    }
}
