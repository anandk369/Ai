package com.mcqautomation.app

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiAPI(private val context: Context) {

    companion object {
        private const val API_KEY = BuildConfig.GEMINI_API_KEY // ✅ Fixed API key reference
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
        private const val TAG = "GeminiAPI"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val answerCache = AnswerCache(context)

    suspend fun getAnswer(questionText: String): String = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cachedAnswer = answerCache.getAnswer(questionText)
            if (cachedAnswer != null) {
                Log.d(TAG, "Retrieved answer from cache")
                return@withContext cachedAnswer
            }

            // Parse MCQ from text
            val mcq = parseMCQ(questionText)
            if (mcq == null) {
                Log.e(TAG, "Failed to parse MCQ from text")
                return@withContext ""
            }

            // Create request
            val prompt = createPrompt(mcq)
            val requestBody = createRequestBody(prompt)

            val request = Request.Builder()
                .url("$BASE_URL?key=$API_KEY")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            // Execute request
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                val answer = extractAnswer(apiResponse)

                // Cache the answer
                if (answer.isNotEmpty()) {
                    answerCache.saveAnswer(questionText, answer)
                }

                Log.d(TAG, "API call successful, answer: $answer")
                return@withContext answer
            } else {
                Log.e(TAG, "API call failed: ${response.code} - $responseBody")
                return@withContext ""
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting answer from Gemini API", e)
            return@withContext ""
        }
    }

    private fun parseMCQ(text: String): MCQ? {
        try {
            // Simple regex to extract question and options
            val lines = text.split("\n").filter { it.trim().isNotEmpty() }

            var question = ""
            val options = mutableMapOf<String, String>()

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.matches(Regex("^[Aa][.):]\\s*(.+)")) -> {
                        options["A"] = trimmed.substring(3).trim()
                    }
                    trimmed.matches(Regex("^[Bb][.):]\\s*(.+)")) -> {
                        options["B"] = trimmed.substring(3).trim()
                    }
                    trimmed.matches(Regex("^[Cc][.):]\\s*(.+)")) -> {
                        options["C"] = trimmed.substring(3).trim()
                    }
                    trimmed.matches(Regex("^[Dd][.):]\\s*(.+)")) -> {
                        options["D"] = trimmed.substring(3).trim()
                    }
                    else -> {
                        if (question.isEmpty() && !trimmed.matches(Regex("^[A-Da-d][.):].*"))) {
                            question = trimmed
                        }
                    }
                }
            }

            return if (question.isNotEmpty() && options.size >= 2) {
                MCQ(question, options)
            } else null

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing MCQ", e)
            return null
        }
    }

    private fun createPrompt(mcq: MCQ): String {
        return buildString {
            append("Answer this multiple choice question by selecting the correct option letter (A, B, C, or D).\n\n")
            append("Question: ${mcq.question}\n\n")
            append("Options:\n")
            mcq.options.forEach { (letter, text) ->
                append("$letter. $text\n")
            }
            append("\nRespond with only the letter of the correct answer (A, B, C, or D).")
        }
    }

    private fun createRequestBody(prompt: String): okhttp3.RequestBody {
        val requestData = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            ),
            "generationConfig" to mapOf(
                "temperature" to 0.1,
                "maxOutputTokens" to 10
            )
        )

        val json = gson.toJson(requestData)
        return json.toRequestBody("application/json".toMediaType())
    }

    private fun extractAnswer(response: GeminiResponse): String {
        return try {
            val content = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            val answer = content?.trim()?.uppercase()

            // Validate answer is A, B, C, or D
            if (answer in listOf("A", "B", "C", "D")) {
                answer
            } else {
                Log.w(TAG, "Invalid answer format: $answer")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting answer", e)
            ""
        }
    }
}

// ✅ Fixed data classes with proper structure
data class MCQ(
    val question: String,
    val options: Map<String, String>
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?,
    @SerializedName("finishReason") val finishReason: String?
)

data class Content(
    val parts: List<Part>?
)

data class Part(
    val text: String?
)