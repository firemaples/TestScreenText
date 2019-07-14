package tw.firemaples.onscreenocr.ocr

import android.content.Context
import android.graphics.Rect
import android.os.AsyncTask
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.floatingviews.ResultBox
import tw.firemaples.onscreenocr.utils.ImageFile

object OCRManager {
    val context: Context by lazy { CoreApplication.instance }
    val tessBaseAPI: TessBaseAPI by lazy { TessBaseAPI() }

    private var callback: OnOCRStateChangedListener? = null
    private var lastAsyncTask: AsyncTask<*, *, *>? = null

    private var currentScreenshot: ImageFile? = null
    private var box: Rect? = null

    fun setListener(callback: OnOCRStateChangedListener) {
        this.callback = callback
    }

    fun start(screenshot: ImageFile, box: Rect) {
        this.currentScreenshot = screenshot
        this.box = box

        initOcrEngine()
    }

    fun cancelRunningTask() {
        lastAsyncTask?.cancel(true)
    }

    private fun initOcrEngine() {
        callback?.onInitializing()

        lastAsyncTask = OcrInitAsyncTask(context, onOcrInitAsyncTaskCallback).execute()
    }

    private val onOcrInitAsyncTaskCallback = object :
            OcrInitAsyncTask.OnOcrInitAsyncTaskCallback {
        override fun onOcrInitialized() {
            callback?.onInitialized()
            startTextRecognize()
        }

        override fun showMessage(message: String) {}

        override fun hideMessage() {}
    }

    private fun startTextRecognize() {
        callback?.onRecognizing()

        lastAsyncTask = OcrRecognizeAsyncTask(context,
                currentScreenshot,
                box,
                onTextRecognizeAsyncTaskCallback).execute()
    }

    private val onTextRecognizeAsyncTaskCallback = object :
            OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback {
        override fun onTextRecognizeFinished(result: ResultBox) {
            callback?.onRecognized(result)
        }

        override fun showMessage(message: String) {}

        override fun hideMessage() {}
    }


    interface OnOCRStateChangedListener {
        fun onInitializing()

        fun onInitialized()

        fun onRecognizing()

        fun onRecognized(result: ResultBox)
    }
}