package com.example.navnreadmobile.ui.screen

import RssViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun DetailScreen(
    title: String,
    description: String,
    imageUrl: String,
    link: String,
    viewModel: RssViewModel
) {
    val fullContent by viewModel.articleContent.collectAsState()
    val scrollState = rememberScrollState()
    LaunchedEffect(link) {
        viewModel.loadFullArticle(link)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl?:"",
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (fullContent.isEmpty())
                "Đang tải nội dung... "
            else
                fullContent,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

