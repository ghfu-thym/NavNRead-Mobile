package com.example.navnreadmobile.network

import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.data.SearchItem
import com.example.navnreadmobile.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class RssRepository {
    private val client = OkHttpClient()

    suspend fun fetchRss(link: String): List<RssItem> = withContext(Dispatchers.IO) {
        val url = link
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val xml = response.body?.string() ?: return@withContext emptyList()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputSource = InputSource(StringReader(xml))
        val doc = builder.parse(inputSource)
        val items = doc.getElementsByTagName("item")

        val result = mutableListOf<RssItem>()
        for (i in 0 until items.length) {
            val node = items.item(i) as? Element ?: continue
            val title = node.getElementsByTagName("title").item(0).textContent
            val link = node.getElementsByTagName("link").item(0).textContent
            val pubDate = node.getElementsByTagName("pubDate").item(0).textContent

            val rawDescription = node.getElementsByTagName("description").item(0).textContent
            val docDescription = Jsoup.parse(rawDescription)
            val image = docDescription.select("img").firstOrNull()?.attr("src") ?: "https://via.placeholder.com/300x180?text=No+Image"
            val descText = docDescription.text()

            result.add(
                RssItem(
                    title = title,
                    description = descText,
                    imageUrl = image,
                    link = link,
                    pubDate = pubDate
                )
            )
        }
        result
    }

    suspend fun fetchSearchResults(query: String): List<SearchItem> = withContext(Dispatchers.IO){
        val result = mutableListOf<SearchItem>()
        var url = Constants.SEARCH_URL+query.replace(" ","%20")
        result
    }
}