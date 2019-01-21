package tw.firemaples.onscreenocr.translate;

import android.util.Log;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.firemaples.onscreenocr.CoreApplication;
import tw.firemaples.onscreenocr.utils.UrlFormatter;

/**
 * Created by firemaples on 13/09/2017.
 */

public class GoogleWebApiTranslator {
    private static final Logger logger = LoggerFactory.getLogger(GoogleWebApiTranslator.class);

    private static String SERVICE_GOOGLE_WEB_API = "https://translate.google.com/translate_a/single?client=t&sl=auto&tl={TL}&hl={TL}&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=7&q={TEXT}";

    private static String REGEX_RESULT_MATCHER = "TRANSLATED_TEXT='(.[^\\']*)'";
    private static String REGEX_RESULT_MATCHER2 = "\\[\\[\\[\"([^\"]*)\",";
    private static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36";

    public static void startTranslate(String textToTranslate, String targetLanguage, final OnGoogleTranslateTaskCallback callback) {

//        String lang = Locale.forLanguageTag(targetLanguage).getLanguage();
//        if (lang.equals(Locale.CHINESE.getLanguage())) {
//            lang += "-" + Locale.getDefault().getCountry();
//        }

        String url = UrlFormatter.getFormattedUrl(SERVICE_GOOGLE_WEB_API, textToTranslate, targetLanguage);

        logger.info("GoogleWebApiTranslator start loading url:" + url);

        String userAgent = new WebView(CoreApplication.getInstance()).getSettings().getUserAgentString();
        logger.info("UserAgent: " + userAgent);
        if (userAgent == null || userAgent.trim().length() == 0) {
            userAgent = DEFAULT_USER_AGENT;
        }
        AndroidNetworking.get(url).setPriority(Priority.HIGH)
                .addHeaders("user-agent", userAgent)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Pattern pattern = Pattern.compile(REGEX_RESULT_MATCHER);
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            String translatedText = matcher.group(1);
                            logger.info("GoogleWebApiTranslator: result found:" + translatedText);
                            callback.onTranslated(translatedText);
                        } else {
                            GoogleTranslateResultNotFoundException exception = new GoogleTranslateResultNotFoundException(response);
                            logger.error("GoogleWebApiTranslator: error: " + Log.getStackTraceString(exception));
                            logger.error(response);
                            callback.onError(exception);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        logger.error("GoogleWebApiTranslator: error: " + anError.getMessage() + "(" + anError.getErrorCode() + ")");
                        logger.error(anError.getErrorBody());
                        callback.onError(anError);
                    }
                });
    }

    public interface OnGoogleTranslateTaskCallback {
        void onTranslated(String translatedText);

        void onError(Throwable throwable);
    }

    public static class GoogleTranslateResultNotFoundException extends Exception {
        private String rawHtml;

        public GoogleTranslateResultNotFoundException(String html) {
            super();
            this.rawHtml = html;
        }

        public String getRawHtml() {
            return rawHtml;
        }
    }
}
