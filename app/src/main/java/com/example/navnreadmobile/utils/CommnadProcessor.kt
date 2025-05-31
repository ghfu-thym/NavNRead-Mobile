package com.example.navnreadmobile.utils

import android.content.Context
import androidx.compose.runtime.MutableState



class CommandProcessor(private val context: Context) {
    fun changeMode(command: String, mode: MutableState<Modes>): String {
        // Convert to lowercase to make matching case-insensitive
        val lowercaseCommand = command.lowercase()

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
                    return "Đã chuyển sang chế độ đọc tin theo chủ đề"
                }
                else -> return "Không nhận diện được chế độ yêu cầu"
            }
        }
        return "Không phải lệnh chuyển chế độ"
    }
}