package com.example.navnreadmobile.ui.screen

import RssViewModel
import android.net.Uri
import android.service.autofill.OnClickAction
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.utils.Constants

@Composable
fun MainScreen(navController: NavController, viewModel: RssViewModel) {
    val news by viewModel.rssItems.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()

    LaunchedEffect(Unit) {
        if(news.isEmpty())
        viewModel.loadNews("https://vnexpress.net/rss/tin-moi-nhat.rss", "Tin mới nhất")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ){
    CategoryBar(
        viewModel = viewModel,
        currentCategory = currentCategory,
        onCategoryClick = {
            categoryKey ->
            val url  = Constants.CATEGORY_MAP[categoryKey]?: ""
            viewModel.loadNews(url,categoryKey)
        }
    )
    Spacer(modifier = Modifier.padding(8.dp))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        items(items = news) { item ->
            ArticleHeadline(
                rssItem = item,
                navController = navController
            )
            HorizontalDivider()
        }
    }
    }
}

@Composable
fun CategoryBar(
    viewModel: RssViewModel,
    onCategoryClick: (String) -> Unit = {},
    currentCategory: String
){
    val categoryList: List<String> = Constants.CATEGORY_MAP.keys.toList()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        items(categoryList){
            item ->
            Button(
                onClick = {
                    onCategoryClick(item)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(item == currentCategory) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                )
            ) {
                Text(
                    text = item
                )
            }
        }
    }
}

@Composable
fun ArticleHeadline(
    rssItem: RssItem,
    navController: NavController,
){
    Card(
        modifier = Modifier
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            navController.navigate(
                "detail/" +
                        Uri.encode(rssItem.title) + "/" +
                        Uri.encode(rssItem.description) + "/" +
                        Uri.encode(rssItem.imageUrl) + "/" +
                        Uri.encode(rssItem.link)
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding( start = 8.dp)
            ) {
                Text(
                    text = rssItem.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3
                )
                Text(
                    text = rssItem.pubDate,
                    maxLines = 1,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
            AsyncImage(
                model = rssItem.imageUrl?:"",
                contentDescription = "Article Image",
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,

            )
        }
    }
}