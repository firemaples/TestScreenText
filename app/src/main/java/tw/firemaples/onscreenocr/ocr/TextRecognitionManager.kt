package tw.firemaples.onscreenocr.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.mlkit.MLKitTextRecognitionManager
import tw.firemaples.onscreenocr.ocr.tesseract.TesseractTextRecognitionEngine
import tw.firemaples.onscreenocr.utils.threadIO
import java.io.File
import java.lang.Exception

object TextRecognitionManager {
    private val latinBasedLanguages: List<String> = listOf()

    private fun getTextRecognitionEngine(lang: String): ITextRecognitionEngine =
            if (latinBasedLanguages.contains(lang)) MLKitTextRecognitionManager
            else TesseractTextRecognitionEngine

    private fun getTextRecognitionEngineFallback(current: ITextRecognitionEngine): ITextRecognitionEngine? =
            if (current is MLKitTextRecognitionManager) TesseractTextRecognitionEngine else null

    fun recognize(imageFile: File, userSelectedRect: Rect, lang: String, onSuccess: (text: String, textBoxes: List<Rect>) -> Unit, onFailed: (Throwable) -> Unit) {
        threadIO {
            val fullBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val croppedBitmap = Bitmap.createBitmap(fullBitmap, userSelectedRect.left, userSelectedRect.top, userSelectedRect.width(), userSelectedRect.height())
            fullBitmap.recycle()
            val engine = getTextRecognitionEngine(lang)
            engine.recognize(
                    croppedBitmap = croppedBitmap,
                    lang = lang,
                    success = { text, textBoxes ->
                        croppedBitmap.recycle()
                        onSuccess(text, textBoxes)
                    },
                    failed = { throwable ->
                        FirebaseEvent.logOCRFailed(engine = engine.javaClass.simpleName, throwable)
                        val fallback = getTextRecognitionEngineFallback(engine)
                        if (fallback != null) {
                            FirebaseEvent.logOCRFallback(from = engine.javaClass.simpleName,
                                    to = fallback.javaClass.simpleName)
                            fallback.recognize(
                                    croppedBitmap = croppedBitmap,
                                    lang = lang,
                                    success = { text, textBoxes ->
                                        croppedBitmap.recycle()
                                        onSuccess(text, textBoxes)
                                    }, failed = { t ->
                                FirebaseEvent.logOCRFailed(engine = fallback.javaClass.simpleName, t)
                                onFailed(t)
                            })
                        } else {
                            croppedBitmap.recycle()
                            onFailed(throwable)
                        }
                    }
            )
        }
    }

    interface ITextRecognitionEngine {
        fun recognize(croppedBitmap: Bitmap, lang: String, failed: ((Throwable) -> Unit)? = null, success: (text: String, textBoxes: List<Rect>) -> Unit)
    }

//    interface TextRecognitionCallback {
//        fun initializing()
//        fun initialized()
//        fun onRecognizing()
//        fun onRecognized(text: String, textBoxes: List<Rect>)
//    }
}