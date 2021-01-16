package tw.firemaples.onscreenocr.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import tw.firemaples.onscreenocr.ocr.tesseract.OcrResult
import tw.firemaples.onscreenocr.utils.ImageFile
import java.io.File

object TextRecognitionManager {
    fun recognize(imageFile: ImageFile, rect: Rect) {
        val fullBitmap = BitmapFactory.decodeFile(imageFile.file.absolutePath)
        val croppedBitmap = Bitmap.createBitmap(fullBitmap, rect.left, rect.top, rect.width(), rect.height())


    }

    interface ITextRecognitionEngine {
        fun recognize(bitmap: Bitmap)
    }

    interface TextRecognitionCallback {
        fun initializing()
        fun initialized()
        fun onRecognizing()
        fun onRecognized(ocrResult: OcrResult)
    }
}