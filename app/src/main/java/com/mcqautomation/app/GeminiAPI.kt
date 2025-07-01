package com.mcqautomation.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GeminiAPI(private val context: Context) {

    companion object {
        private const val API_KEY = "your_api_key_here" // Replace with actual API key
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"
    }

    private val client = OkHttpClient()

    suspend fun getAnswer(questionText: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(questionText)
                val response = makeAPICall(prompt)
                parseResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    private fun buildPrompt(questionText: String): String {
        return """
            You are an expert at answering multiple choice questions. 
            Analyze the following MCQ and provide ONLY the letter of the correct answer (A, B, C, or D).
            Do not provide explanations, just the single letter.

            Question:
            $questionText
        """.trimIndent()
    }

    private suspend fun makeAPICall(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("maxOutputTokens", 10)
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL?key=$API_KEY")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: ""
            } else {
                throw IOException("API call failed: ${response.code}")
            }
        }
    }

    private fun parseResponse(response: String): String {
        return try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val text = parts.getJSONObject(0).getString("text").trim()
                    // Extract just the letter (A, B, C, or D)
                    val answer = text.uppercase().firstOrNull { it in 'A'..'D' }
                    answer?.toString() ?: ""
                } else ""
            } else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}