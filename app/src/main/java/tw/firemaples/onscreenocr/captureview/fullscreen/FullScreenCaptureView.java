package tw.firemaples.onscreenocr.captureview.fullscreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.Arrays;
import java.util.List;

import tw.firemaples.onscreenocr.OnScreenTranslateService;
import tw.firemaples.onscreenocr.captureview.CaptureView;
import tw.firemaples.onscreenocr.orc.OrcInitAsyncTask;
import tw.firemaples.onscreenocr.orc.OrcResult;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.SettingsActivity;
import tw.firemaples.onscreenocr.orc.OrcRecognizeAsyncTask;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.translate.TranslateAsyncTask;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by Firemaples on 2016/3/1.
 */
public class FullScreenCaptureView extends CaptureView {
    private static FullScreenCaptureView fullScreenCaptureView;

    private Context context;
    private Handler handler = new Handler();
    private OnScreenTranslateService onScreenTranslateService;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private View rootView;
    private FullScreenCaptureAreaSelectionView fullScreenCaptureAreaSelectionView;
    private FullScreenOrcResultsView fullScreenOrcResultsView;
    private View bt_captureViewPageMinimize, bt_captureViewPageClearAll, bt_captureViewPageTranslate, bt_captureViewPageSettings, bt_captureViewPageReselect;
    private View view_progress;
    private TextView tv_progressMsg;

    private boolean isShown = false;
    private boolean isProgressing = false;

    private TessBaseAPI baseAPI;

    private Bitmap currentScreenshot;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_captureViewPageMinimize) {
                hideView();
                onScreenTranslateService.showFloatingView();
            } else if (id == R.id.bt_captureViewPageClearAll) {
                fullScreenCaptureAreaSelectionView.clear();
                fullScreenCaptureAreaSelectionView.setVisibility(View.GONE);
                fullScreenCaptureAreaSelectionView.setVisibility(View.VISIBLE);
            } else if (id == R.id.bt_captureViewPageTranslate) {
                if (fullScreenCaptureAreaSelectionView.getBoxList().size() == 0) {
                    Tool.LogError("Please draw area before recognize");
                    Tool.ShowErrorMsg(context, "Please draw area before recognize");
                } else {
                    ScreenshotHandler screenshotHandler = ScreenshotHandler.getInstance(context);
                    if (screenshotHandler.isGetUserPermission()) {
                        takeScreenShot(screenshotHandler);
                    } else {
                        screenshotHandler.getUserPermission();
                    }
                }
            } else if (id == R.id.bt_captureViewPageSettings) {
                context.startActivity(SettingsActivity.getIntent(context)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                hideView();
            } else if (id == R.id.bt_captureViewPageReselect) {
                onModeChange(MODE_SELECTION);
            }
        }
    };

    private ScreenshotHandler.OnScreenshotHandlerCallback onScreenshotHandlerCallback = new ScreenshotHandler.OnScreenshotHandlerCallback() {
        @Override
        public void onScreenshotFinished(Bitmap bitmap) {
            FullScreenCaptureView.this.onScreenshotFinished(bitmap);
        }
    };

    private OrcInitAsyncTask.OnOrcInitAsyncTaskCallback onOrcInitAsyncTaskCallback = new OrcInitAsyncTask.OnOrcInitAsyncTaskCallback() {
        @Override
        public void onOrcInitialized() {
            FullScreenCaptureView.this.onOrcInitialized();
        }
    };

    private OrcRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback onTextRecognizeAsyncTaskCallback = new OrcRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback() {
        @Override
        public void onTextRecognizeFinished(List<OrcResult> results) {
            FullScreenCaptureView.this.onTextRecognizeFinished(results);
        }
    };

    private TranslateAsyncTask.OnTranslateAsyncTaskCallback onTranslateAsyncTaskCallback = new TranslateAsyncTask.OnTranslateAsyncTaskCallback() {
        @Override
        public void onTranslateFinished(List<OrcResult> translatedResult) {
            FullScreenCaptureView.this.onTranslateFinished(translatedResult);
        }
    };

    public static FullScreenCaptureView getNewInstance(Context context, OnScreenTranslateService onScreenTranslateService) {
        if (fullScreenCaptureView == null)
            fullScreenCaptureView = new FullScreenCaptureView(context, onScreenTranslateService);
        return fullScreenCaptureView;
    }

    public static FullScreenCaptureView getCurrentInstance() {
        return fullScreenCaptureView;
    }

    private FullScreenCaptureView(Context context, OnScreenTranslateService onScreenTranslateService) {
        this.context = context;
        this.onScreenTranslateService = onScreenTranslateService;
        this.rootView = View.inflate(context, R.layout.view_full_screen_capture_page, null);
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        iniViews();
    }

    private void iniViews() {
        bt_captureViewPageMinimize = rootView.findViewById(R.id.bt_captureViewPageMinimize);
        bt_captureViewPageClearAll = rootView.findViewById(R.id.bt_captureViewPageClearAll);
        bt_captureViewPageTranslate = rootView.findViewById(R.id.bt_captureViewPageTranslate);
        bt_captureViewPageSettings = rootView.findViewById(R.id.bt_captureViewPageSettings);
        bt_captureViewPageReselect = rootView.findViewById(R.id.bt_captureViewPageReselect);
        view_progress = rootView.findViewById(R.id.view_progress);
        tv_progressMsg = (TextView) rootView.findViewById(R.id.tv_progressMsg);

        bt_captureViewPageMinimize.setOnClickListener(onClickListener);
        bt_captureViewPageClearAll.setOnClickListener(onClickListener);
        bt_captureViewPageTranslate.setOnClickListener(onClickListener);
        bt_captureViewPageSettings.setOnClickListener(onClickListener);
        bt_captureViewPageReselect.setOnClickListener(onClickListener);
        fullScreenCaptureAreaSelectionView = (FullScreenCaptureAreaSelectionView) rootView.findViewById(R.id.fullScreenCaptureAreaSelectionView);
        fullScreenOrcResultsView = (FullScreenOrcResultsView) rootView.findViewById(R.id.fullScreenOrcResultsView);

        onModeChange(MODE_SELECTION);
        setProgressMode(false, null);
    }

    @Override
    public void showView() {
        if (!isShown) {
            isShown = true;
            windowManager.addView(rootView, params);
        }
    }

    @Override
    public void hideView() {
        if (isShown) {
            isShown = false;
            windowManager.removeView(rootView);
        }
    }

    @Override
    public void setProgressMode(boolean progress, String message) {
        this.isProgressing = progress;
        if (progress) {
            view_progress.setVisibility(View.VISIBLE);
            tv_progressMsg.setText(message == null ? context.getString(R.string.progressProcessingDefaultMessage) : message);
            if (message != null) Tool.LogInfo("setProgressMode: " + message);
        } else {
            view_progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onModeChange(int mode) {
        switch (mode) {
            case MODE_SELECTION:
                fullScreenCaptureAreaSelectionView.setVisibility(View.VISIBLE);
                bt_captureViewPageClearAll.setVisibility(View.VISIBLE);
                bt_captureViewPageTranslate.setVisibility(View.VISIBLE);
                bt_captureViewPageSettings.setVisibility(View.VISIBLE);

                fullScreenOrcResultsView.setVisibility(View.GONE);
                bt_captureViewPageReselect.setVisibility(View.GONE);
                break;
            case MODE_RESULT:
                fullScreenCaptureAreaSelectionView.setVisibility(View.GONE);
                bt_captureViewPageClearAll.setVisibility(View.GONE);
                bt_captureViewPageTranslate.setVisibility(View.GONE);
                bt_captureViewPageSettings.setVisibility(View.GONE);

                fullScreenOrcResultsView.setVisibility(View.VISIBLE);
                bt_captureViewPageReselect.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void takeScreenShot(final ScreenshotHandler screenshotHandler) {
        setProgressMode(true, "Taken a screenshot...");
        screenshotHandler.setCallback(onScreenshotHandlerCallback);

        onScreenTranslateService.hideFloatingView();

        hideView();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                screenshotHandler.takeScreenshot();
            }
        }, 100);
    }

    public void onScreenshotFinished(Bitmap bitmap) {
        setProgressMode(true, "A screenshot has been taken.");
        this.currentScreenshot = bitmap;
        onScreenTranslateService.hideFloatingView();
        showView();
//        ImageView imageView = (ImageView) rootView.findViewById(R.id.iv_screenshotPreview);
//        imageView.setImageBitmap(bitmap);
        initOrcEngine();
    }

    public void initOrcEngine() {
        setProgressMode(true, "Waiting for Orc engine initialize...");
        if (baseAPI == null) {
            baseAPI = new TessBaseAPI();
        }

        String recognitionLang = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.KEY_RECOGNITION_LANGUAGE, "eng");

        int langIndex = Arrays.asList(context.getResources().getStringArray(R.array.iso6393)).indexOf(recognitionLang);
        String langName = context.getResources().getStringArray(R.array.languagenames)[langIndex];

        new OrcInitAsyncTask(context, baseAPI, recognitionLang, langName, this).setCallback(onOrcInitAsyncTaskCallback).execute();
    }

    public void onOrcInitialized() {
        setProgressMode(true, "Orc engine initialized.");
        startTextRecognize();
    }

    public void startTextRecognize() {
        setProgressMode(true, "Start text recognition...");
        List<Rect> boxList = fullScreenCaptureAreaSelectionView.getBoxList();
        new OrcRecognizeAsyncTask(context, baseAPI, this, currentScreenshot, boxList).setCallback(onTextRecognizeAsyncTaskCallback).execute();
    }

    public void onTextRecognizeFinished(List<OrcResult> results) {
        startTranslate(results);
    }

    public void startTranslate(List<OrcResult> results) {
        new TranslateAsyncTask(context, this, results).setCallback(onTranslateAsyncTaskCallback).execute();
    }

    public void onTranslateFinished(List<OrcResult> translatedResult) {
        setProgressMode(false, null);
        Tool.ShowMsg(context, "TextRecognizeFinished!");
        onModeChange(MODE_RESULT);
        fullScreenOrcResultsView.setOrcResults(translatedResult);
    }
}