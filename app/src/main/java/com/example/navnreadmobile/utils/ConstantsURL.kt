package com.example.navnreadmobile.utils

object ConstantsURL {
    val CATEGORY_MAP: Map<String, String> = mapOf(
        "Tin mới nhất" to "https://vnexpress.net/rss/tin-moi-nhat.rss",
        "Thời sự" to "https://vnexpress.net/rss/thoi-su.rss",
        "Thế giới" to "https://vnexpress.net/rss/the-gioi.rss",
        "Kinh doanh" to "https://vnexpress.net/rss/kinh-doanh.rss",
        "Giải trí" to "https://vnexpress.net/rss/giai-tri.rss",
        "Thể thao" to "https://vnexpress.net/rss/the-thao.rss",
        "Pháp luật" to "https://vnexpress.net/rss/phap-luat.rss",
        "Sức khỏe" to "https://vnexpress.net/rss/suc-khoe.rss",
        "Giáo dục" to "https://vnexpress.net/rss/giao-duc.rss",
        "Du lịch" to "https://vnexpress.net/rss/du-lich.rss",
        "Xe cộ" to "https://vnexpress.net/rss/oto-xe-may.rss",
        "Đời sống" to "https://vnexpress.net/rss/gia-dinh.rss",
        "Công nghệ" to "https://vnexpress.net/rss/khoa-hoc-cong-nghe.rss",
    )
    const val SEARCH_URL = "https://timkiem.vnexpress.net/?q="


}

object VoiceCommands {
    const val NEWEST_NEWS = "tin mới nhất"
    const val SEARCH_NEWS = "tìm kiếm"
    const val CATEGORY_MODE = "chủ đề"
    const val READ_ALOUD = "đọc tin"
    const val STOP_READING = "dừng đọc"
    const val RESUME_READING = "đọc tiếp"
    const val REFRESH_NEWS = "làm mới tin tức"
    const val NEXT_NEWS = "tin tiếp theo"
    const val PREVIOUS_NEWS = "tin trước"
}

enum class Modes {
    CATEGORY,
    SEARCH,
    NEWEST_NEWS,
}