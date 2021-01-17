package tw.firemaples.onscreenocr.ocr.tesseract

import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.ocr.TextRecognitionManager
import java.io.File

object TesseractTextRecognitionEngine : TextRecognitionManager.ITextRecognitionEngine {
    private val baseAPI: TessBaseAPI by lazy { TessBaseAPI() }

    private val tessRootDir: File
        get() = OCRFileUtil.tessDataBaseDir

    private val tessPageMode: Int
    get() = OCRLangUtil.pageSegmentationModeIndex

    private fun initialize(lang: String) {
        baseAPI.init(tessRootDir.absolutePath, lang, TessBaseAPI.OEM_DEFAULT)
        baseAPI.pageSegMode = tessPageMode
    }

    override fun recognize(bitmap: Bitmap, lang: String, callback: TextRecognitionManager.TextRecognitionCallback) {


        callback.initializing()
        initialize(lang)
        callback.initialized()
    }
}