import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.network.RssRepository
import com.example.navnreadmobile.utils.ConstantsURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RssViewModel : ViewModel() {
    private val repository = RssRepository()

    private val _rssItems = MutableStateFlow<List<RssItem>>(emptyList())
    val rssItems: StateFlow<List<RssItem>> = _rssItems

    private val _currentCategory = MutableStateFlow("Tin mới nhất")
    val currentCategory: StateFlow<String> = _currentCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _articleContent = MutableStateFlow<String>("")
    val articleContent: StateFlow<String> get() = _articleContent

    private val _articleSummary = MutableStateFlow<String>("")
    val articleSummary: StateFlow<String> get() = _articleSummary

    private val _searchResults = MutableStateFlow<List<RssItem>>(emptyList())
    val searchResults: StateFlow<List<RssItem>> = _searchResults

    fun loadNews(url:String, category: String? = null) {
        category?.let{ _currentCategory.value = it}
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _rssItems.value = repository.fetchRss(url)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFullArticle(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(url).get()

                Log.d("Jsoup", "HTML: ${document.html().take(500)}")
                val article = document.selectFirst("article.fck_detail ")
                if (article == null) {
                    Log.e("Jsoup", "Can't find article.fck_detail")
                } else {
                    Log.d("Jsoup", "Tìm thấy phần article")
                }
                val content = article?.select("p.Normal")?.joinToString("\n") { it.text() }
                    ?: "Không tìm thấy nội dung bài báo"
                Log.d("Jsoup", content)

                _articleContent.value = content
            } catch (e: Exception) {
                _articleContent.value = "Lỗi tải bài báo: ${e.message}"
            }
        }
    }

    fun updateSummary(summary: String) {
        _articleSummary.value = summary
    }

    fun clearSummary() {
        _articleSummary.value = ""
    }

    fun loadSearchResults(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _searchResults.value = emptyList() // Reset search results
                val url = ConstantsURL.SEARCH_URL + query
                val document = Jsoup.connect(url).get()

                val articles = document.select("article.item-news")
                if (articles.isEmpty()) {
                    Log.e("Jsoup", "Can't find article.item-news")
                } else {
                    Log.d("Jsoup", "Tìm thấy phần article")
                }

                for(article in articles) {
                    val titleElement = article.selectFirst("h3.title-news a")
                    val descriptionElement = article.selectFirst("p.description")
                    val link = article.attr("data-url")

                    val sourceElement = article.selectFirst("source")
                    val imageUrl = sourceElement?.attr("data-srcset")?.split(" ")?.firstOrNull()
                        ?: "https://via.placeholder.com/300x180?text=No+Image"

                    val publishTimestamp = article.attr("data-publishtime").toLongOrNull()
                    val publishDate = publishTimestamp?.let {
                        Instant.ofEpochSecond(it)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                    } ?: "Không rõ"


                    val title = titleElement?.text() ?: ""
                    val description = descriptionElement?.text() ?: "No description"
                    if(title.isNotEmpty()) {
                        _searchResults.value += RssItem(
                            title = title,
                            description = description,
                            link = link,
                            imageUrl = imageUrl,
                            pubDate = publishDate
                        )
                    }
                }
            } catch (e: Exception){
                _searchResults.value += RssItem(
                    title = "Lỗi tải kết quả tìm kiếm",
                    description = e.message ?: "Không rõ lỗi",
                    link = "",
                    imageUrl = "",
                    pubDate = ""
                )
            }
        }
    }

}
