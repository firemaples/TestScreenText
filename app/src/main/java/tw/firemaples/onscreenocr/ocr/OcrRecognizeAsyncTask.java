package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.DebugInfo;
import tw.firemaples.onscreenocr.floatingviews.ResultBox;
import tw.firemaples.onscreenocr.utils.ImageFile;
import tw.firemaples.onscreenocr.utils.SettingUtil;
import tw.firemaples.onscreenocr.utils.Utils;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, String, ResultBox> {
    private static final Logger logger = LoggerFactory.getLogger(OcrRecognizeAsyncTask.class);

    private final WeakReference<Context> contextRef;
    private final TessBaseAPI baseAPI;
    private ImageFile screenshot;
    private final Rect box;

    private static final int textMargin = 10;

    private OnTextRecognizeAsyncTaskCallback callback;

    public OcrRecognizeAsyncTask(Context context, ImageFile screenshot, Rect box, OnTextRecognizeAsyncTaskCallback callback) {
        this.contextRef = new WeakReference<>(context);
        this.screenshot = screenshot;
        this.box = box;
        this.callback = callback;

        this.baseAPI = OCRManager.INSTANCE.getTessBaseAPI();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate(contextRef.get().getString(R.string.progress_textRecognizing));

        if (callback == null) {
            throw new UnsupportedOperationException("Callback is not implemented");
        }
    }

    @Override
    protected ResultBox doInBackground(Void... params) {
        baseAPI.setImage(ReadFile.readFile(screenshot.getFile()));

        WindowManager wm = (WindowManager) contextRef.get().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        ResultBox resultBox = new ResultBox();

        //Try to fix sides of rect
        fixRect(box, screenshot.getWidth(), screenshot.getHeight());

        baseAPI.setRectangle(box);
        resultBox.setRect(box);
        String resultText = baseAPI.getUTF8Text();
        if (SettingUtil.INSTANCE.getRemoveLineBreaks()) {
            resultText = Utils.replaceAllLineBreaks(resultText, " ");
        }
        resultBox.setText(resultText);
        resultBox.setBoxRects(baseAPI.getRegions().getBoxRects());
        resultBox.setResultIterator(baseAPI.getResultIterator());

        if (resultBox.getBoxRects().size() > 0) {
            Rect boxRect = resultBox.getBoxRects().get(0);

            Rect subRect = new Rect(box.left + boxRect.left - textMargin,
                    box.top + boxRect.top - textMargin,
                    box.left + boxRect.right + textMargin,
                    box.top + boxRect.bottom + textMargin);
            resultBox.setSubRect(subRect);
        }

        if (SettingUtil.INSTANCE.isDebugMode()) {
            DebugInfo debugInfo = new DebugInfo();
            Bitmap fullBitmap = BitmapFactory.decodeFile(screenshot.getFile().getAbsolutePath());
            Bitmap cropped = Bitmap.createBitmap(fullBitmap, box.left, box.top, box.width(), box.height());
            fullBitmap.recycle();
            debugInfo.setCroppedBitmap(cropped);
            debugInfo.addInfoString(String.format(Locale.getDefault(), "Screen size:%dx%d", metrics.widthPixels, metrics.heightPixels));
            debugInfo.addInfoString(String.format(Locale.getDefault(), "Screenshot size:%dx%d", screenshot.getWidth(), screenshot.getHeight()));
            debugInfo.addInfoString(String.format(Locale.getDefault(), "Cropped position:%s", box.toString()));
            debugInfo.addInfoString(String.format(Locale.getDefault(), "OCR result:%s", resultBox.getText()));
            resultBox.setDebugInfo(debugInfo);
        }

        return resultBox;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (callback != null) {
            callback.showMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(ResultBox result) {
        super.onPostExecute(result);
        logger.info("OCR result:" + result.getText());
        if (callback != null) {
            callback.hideMessage();
            callback.onTextRecognizeFinished(result);
        }
    }

    private void fixRect(Rect rect, int bitmapWidth, int bitmapHeight) {
        if (rect.left < 0) {
            rect.left = 0;
        }
        if (rect.top < 0) {
            rect.top = 0;
        }
        if (rect.right > bitmapWidth) {
            rect.right = bitmapWidth;
        }
        if (rect.bottom > bitmapHeight) {
            rect.bottom = bitmapHeight;
        }
    }

    public interface OnTextRecognizeAsyncTaskCallback {
        void onTextRecognizeFinished(ResultBox resultBox);

        void showMessage(String message);

        void hideMessage();
    }
}
