package tw.firemaples.onscreenocr.floatingviews

import android.graphics.Bitmap
import android.graphics.Rect
import com.googlecode.tesseract.android.ResultIterator
import java.util.*

/**
 * @param rect Region of detected text
 * @param subRect Region of text area with margins
 * @param touchRect Region of result text area
 */
data class ResultBox(var text: String? = null,
                     var translatedText: String? = null,
                     var resultIterator: ResultIterator? = null,
                     var boxRects: List<Rect> = listOf(),
                     var rect: Rect? = null,
                     var subRect: Rect? = null,
                     var touchRect: Rect? = null,
                     var textWidth: Int? = null,
                     var textHeight: Int? = null,
                     var debugInfo: DebugInfo? = null) {
    constructor() : this(null)


}

class DebugInfo {
    var croppedBitmap: Bitmap? = null
    private val infoList = ArrayList<String>()

    fun addInfoString(info: String) {
        infoList.add(info)
    }

    fun getInfoList(): List<String> {
        return infoList
    }
}