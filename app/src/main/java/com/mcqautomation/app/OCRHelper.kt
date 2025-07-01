` tags. I'll pay close attention to preserving the original structure and indentation while incorporating the changes from the edited snippet.

```
<replit_final_file>
package com.mcqautomation.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.projection.MediaProjection
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRHelper(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun captureAndExtractText(
        mediaProjection: MediaProjection,
        captureRegion: Rect,
        resolution: String
    ): String {
        return try {
            // For now, return a sample MCQ text since screen capture implementation
            // requires additional MediaProjection setup
            Log.d("OCRHelper", "Simulating screen capture and OCR")

            // Simulate processing time
            Thread.sleep(300)

            // Return sample MCQ for testing
            """
            What is the capital of France?
            A) London
            B) Berlin
            C) Paris
            D) Madrid
            """.trimIndent()

        } catch (e: Exception) {
            Log.e("OCRHelper", "Error in OCR processing", e)
            ""
        }
    }

    private suspend fun processImageWithMLKit(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            Log.e("OCRHelper", "ML Kit processing failed", e)
            ""
        }
    }

    fun parseMCQText(text: String): MCQData? {
        return try {
            val lines = text.split("\n").filter { it.trim().isNotEmpty() }
            if (lines.isEmpty()) return null

            val question = lines.firstOrNull { !it.trim().matches(Regex("^[A-D]\\).*")) } ?: ""
            val options = mutableMapOf<String, String>()

            for (line in lines) {
                val optionMatch = Regex("^([A-D])\\)\\s*(.+)").find(line.trim())
                optionMatch?.let {
                    options[it.groupValues[1]] = it.groupValues[2].trim()
                }
            }

            if (question.isNotBlank() && options.isNotEmpty()) {
                MCQData(question.trim(), options)
            } else null

        } catch (e: Exception) {
            Log.e("OCRHelper", "Error parsing MCQ text", e)
            null
        }
    }
}

data class MCQData(
    val question: String,
    val options: Map<String, String>
)