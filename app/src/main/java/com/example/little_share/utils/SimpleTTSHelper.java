package com.example.little_share.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class SimpleTTSHelper {
    private static final String TAG = "SimpleTTS";
    private static TextToSpeech tts;
    private static boolean isInitialized = false;
    
    public static void init(Context context) {
        if (tts == null) {
            Log.d(TAG, "Initializing TTS...");
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(0.9f);
                    tts.setPitch(1.2f);
                    isInitialized = true;
                    Log.d(TAG, "TTS initialized successfully");
                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            });
        }
    }
    
    public static void speakLittleShare() {
        if (tts != null && isInitialized) {
            Log.d(TAG, "Speaking: Little Share");
            tts.speak("Little Share", TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Log.w(TAG, "TTS not ready");
        }
    }
    
    public static void destroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInitialized = false;
        }
    }
}