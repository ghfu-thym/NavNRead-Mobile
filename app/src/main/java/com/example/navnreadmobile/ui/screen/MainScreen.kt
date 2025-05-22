package com.example.navnreadmobile.ui.screen

import RssViewModel
import android.net.Uri
import android.service.autofill.OnClickAction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.navnreadmobile.utils.Constants

@Composable
fun MainScreen(navController: NavController, viewModel: RssViewModel) {
    val news by viewModel.rssItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNews("https://vnexpress.net/rss/tin-moi-nhat.rss")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ){
    CategoryBar(
        viewModel = viewModel,
        onCategoryClick = {
            categoryKey ->
            viewModel.loadNews(Constants.CATEGORY_MAP[categoryKey] ?: "")
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
            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            "detail/" +
                                    Uri.encode(item.title) + "/" +
                                    Uri.encode(item.description) + "/" +
                                    Uri.encode(item.imageUrl) + "/" +
                                    Uri.encode(item.link)
                        )
                    }
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
        }
    }
    }
}

@Composable
fun CategoryBar(
    viewModel: RssViewModel,
    onCategoryClick: (String) -> Unit = {}
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
                }
            ) {
                Text(
                    text = item
                )
            }
        }
    }
}