package tw.firemaples.onscreenocr.ocr.tesseract

import android.graphics.Bitmap
import android.graphics.Rect
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.ocr.TextRecognitionManager
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.Utils
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

    override fun recognize(croppedBitmap: Bitmap, lang: String, failed: ((Throwable) -> Unit)?, success: (text: String, textBoxes: List<Rect>) -> Unit) {
        initialize(lang)

        baseAPI.setImage(croppedBitmap)
        var resultText = baseAPI.utF8Text ?: ""

        if (SettingUtil.removeLineBreaks) {
            resultText = Utils.replaceAllLineBreaks(resultText, " ") ?: ""
        }

        val textBoxes = baseAPI.regions.boxRects

        success(resultText, textBoxes)
    }
}
