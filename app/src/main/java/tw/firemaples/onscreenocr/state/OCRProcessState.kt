package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.floatingviews.ResultBox
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.OCRManager
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.Utils

object OCRProcessState : OverlayState() {

    override fun stateName(): StateName = StateName.OCRProcess

    override fun enter(manager: StateManager) {

        manager.dispatchStartOCR()

        OCRManager.setListener(callback)


        val box = manager.selectedBox
        val screenshotFile = manager.screenshotFile
        if (box != null && screenshotFile != null) {
            manager.resultBox = ResultBox(rect = box)

            OCRManager.start(screenshotFile, box)
        } else {
            throw IllegalArgumentException("Selected box or screenshot file not found, " +
                    "selectedBox: $box, screenshotFile: $screenshotFile")
        }
    }

    val callback = object : OCRManager.OnOCRStateChangedListener {
        override fun onInitializing() {
            FirebaseEvent.logStartOCRInitializing()
            StateManager.dispatchStartOCRInitializing()
        }

        override fun onInitialized() {
            FirebaseEvent.logOCRInitialized()
        }

        override fun onRecognizing() {
            FirebaseEvent.logStartOCR()
            StateManager.dispatchStartOCRRecognizing()
        }

        override fun onRecognized(result: ResultBox) {
            FirebaseEvent.logOCRFinished()
            this@OCRProcessState.onRecognized(result)
        }
    }

    fun onRecognized(result: ResultBox) {
        StateManager.resultBox = result

        val text = result.text
        if (!text.isNullOrBlank() && SettingUtil.autoCopyOCRResult) {
            Utils.copyToClipboard(Utils.LABEL_OCR_RESULT, text)
        }

        StateManager.dispatchOCRRecognized()
        if (TranslationUtil.currentService != TranslationService.GoogleTranslatorApp) {
            StateManager.enterState(TranslatingState)
        } else {
            StateManager.enterState(InitState)
        }
    }
}