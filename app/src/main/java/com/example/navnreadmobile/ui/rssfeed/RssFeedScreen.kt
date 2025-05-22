package com.example.navnreadmobile.ui.rssfeed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.navnreadmobile.data.RssItem
import com.example.navnreadmobile.ui.theme.NavNReadMobileTheme

@Composable
fun RssFeedScreen (
    rssItems: List<RssItem>,
    onItemClick: (RssItem) -> Unit
){
    Text("RSS Feed")
    LazyColumn {
        items(rssItems) { item ->
            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onItemClick(item) }
            )
        }
    }

}

