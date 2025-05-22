package com.example.navnreadmobile.data

data class RssChannel (
    val title : String,
    val link: String,
    val items: List<RssItem>
)