
package com.mcqautomation.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OCRHelper(private val context: Context) {
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    companion object {
        private const val VIRTUAL_DISPLAY_NAME = "MCQ_ScreenCapture"
    }
    
    suspend fun captureAndExtractText(): String {
        return try {
            val bitmap = captureScreen()
            if (bitmap != null) {
                extractTextFromBitmap(bitmap)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    private suspend fun captureScreen(): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels
                val density = displayMetrics.densityDpi
                
                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
                
                imageReader?.setOnImageAvailableListener({ reader ->
                    val image = reader?.acquireLatestImage()
                    if (image != null) {
                        val bitmap = imageToBitmap(image)
                        image.close()
                        continuation.resume(bitmap)
                    } else {
                        continuation.resume(null)
                    }
                }, null)
                
                // For now, return a placeholder bitmap since MediaProjection setup requires Activity context
                // This would need to be properly implemented with MediaProjection in a real scenario
                continuation.resume(createPlaceholderBitmap())
                
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    private fun createPlaceholderBitmap(): Bitmap {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }
    
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
    
    private suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        return suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val extractedText = processVisionText(visionText)
                    continuation.resume(extractedText)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
    
    private fun processVisionText(visionText: Text): String {
        val result = StringBuilder()
        
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                result.append(line.text).append("\n")
            }
        }
        
        return result.toString().trim()
    }
    
    fun cleanup() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        textRecognizer.close()
    }
}
