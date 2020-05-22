package com.example.project;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class MyTextToSpeech {
    private TextToSpeech TTS;
    boolean ttsEnabled = false;

    public MyTextToSpeech(final Context context){

        TTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    if (TTS.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage())) == TextToSpeech.LANG_AVAILABLE)
                    {
                        TTS.setLanguage(new Locale(Locale.getDefault().getLanguage()));
                    } else {
                        TTS.setLanguage(Locale.US);
                    }
                    TTS.setPitch(1.3f);
                    TTS.setSpeechRate(0.7f);
                    ttsEnabled = true;
                }else
                    Toast.makeText(context, "Чёт не то, все... давай по новой", Toast.LENGTH_LONG).show();
            }
//            public void speak(String text){
//                HashMap<String, String> map = new HashMap<>();
//                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
//                TTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);

//            }
        });
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    Log.d("MY_TAG", String.valueOf(TTS.isSpeaking()));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    public void speak(String text){
        String utteranceId = this.hashCode() + "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    public boolean isSpeaking() {
        return TTS.isSpeaking();
    }
}