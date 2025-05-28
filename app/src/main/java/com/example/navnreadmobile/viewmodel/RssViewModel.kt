import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.network.RssRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

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

}
