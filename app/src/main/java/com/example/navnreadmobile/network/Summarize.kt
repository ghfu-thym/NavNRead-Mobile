package com.example.navnreadmobile.network


import com.google.ai.client.generativeai.GenerativeModel
import com.example.navnreadmobile.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Summarize {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
    )

    /**
     * Summarizes an article using Gemini AI model.
     *
     * @param articleContent The content of the article to summarize
     * @return A flow emitting the summarized content as a string
     */
    suspend fun summarize(articleContent: String): String {
        val prompt = "Bạn là một AI hữu ích, có nhiệm vụ tóm tắt bài báo cho người khiếm thị:\n" +
                "1. Chỉ nêu các ý chính quan trọng\n" +
                "2. Sử dụng ngôn ngữ đơn giản, dễ hiểu\n" +
                "3. Chuyển đổi ngày tháng với dấu phẩy hoặc dấu chấm thành dạng đầy đủ (ví dụ: 12/3/2023 thành ngày 12 tháng 3 năm 2023)\n" +
                "4. Chuyển từ viết tắt (ví dụ: LHQ, WHO, TP) thành dạng đầy đủ (Liên Hợp Quốc, Tổ chức Y tế Thế giới, Thành phố)\n" +
                "5. Giữ nguyên các thông tin quan trọng khác\n" +
                "6. Chỉ trả về nội dung tóm tắt, không cần tiêu đề hay bất kỳ thông tin nào khác" +
                "Đây là nội dung bài báo: $articleContent"

        // Generate content with the Gemini model
        val response = generativeModel.generateContent(prompt)

        // Return the text from the response
        return response.text ?: "Unable to generate summary."
    }

    /**
     * Summarizes an article using Gemini AI model and returns a Flow for streaming responses.
     *
     * @param articleContent The content of the article to summarize
     * @return A flow emitting the summarized content as it's generated
     */
    fun summarizeAsFlow(articleContent: String): Flow<String> = flow {
        val prompt =
            "Summarize the following article in a concise way, maintaining the key points: $articleContent"

        // Generate content stream with the Gemini model
        val responseFlow = generativeModel.generateContentStream(prompt)

        // Emit each chunk of the response as it comes in
        responseFlow.collect { chunk ->
            chunk.text?.let { text ->
                emit(text)
            }
        }
    }
}

