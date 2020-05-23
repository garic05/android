package com.example.project;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SeansActivity extends Activity {
    private static final int REQUEST_RECORD_PERMISSION = 100;
    public static final String SERV_TAG = "FIND";
    public static final String CLIENT_TAG = "REC";
    public static final String ERROR_TAG = "ERROR";
    public static final String LOG_TAG = "MY_TAG";
    public static final String REQUEST_TO_SAY = "REQUEST_TO_SAY";
    public static final String CODE_TAG = "CODE";
    public static final String DEVICE_TAG = "DEVICE_TAG";
    public static final String ENABLE_SAY = "ENABLE_SAY";
    public String MY_IP;

    private TextView textViewListen;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private String sendingMessage = "", lastListenMessage = "", partMessage = "";
    private MyTextToSpeech textToSpeech;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private String deviceIs, code, recMessage;
    private Handler handler;
    private Client client;
    private Server server;

    private String lang;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seans);
        {
            textToSpeech = new MyTextToSpeech(this);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listView = findViewById(R.id.listView);
            listView.setAdapter(adapter);
            handler = new Handler();
            handler.post(timerTask);
            code = getIntent().getStringExtra(CODE_TAG);
            deviceIs = getIntent().getStringExtra(DEVICE_TAG);
            lang = getIntent().getStringExtra("lang");
            Log.d(LOG_TAG, "seans lang =" + lang);

            toggleButton = findViewById(R.id.toggle_button);
            progressBar = findViewById(R.id.progressBar);
            textViewListen = findViewById(R.id.textResult);
            textViewListen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textViewListen.setText("");
                }
            });

            toggleButton.setOnCheckedChangeListener(chekedChangeListener);
            progressBar.setVisibility(View.INVISIBLE);
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new listener());
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
            Locale.setDefault(Locale.forLanguageTag(lang));

            SpannableString content = new SpannableString("X");
            content.setSpan(new ImageSpan(this, R.drawable.micro_on), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            toggleButton.setTextOn(content);
            SpannableString spannableString = new SpannableString("X");
            spannableString.setSpan(new ImageSpan(this, R.drawable.micro_off), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            toggleButton.setTextOff(spannableString);
            toggleButton.setChecked(false);

        }
        try {
            switch (deviceIs) {
                case SERV_TAG:
                    server = new Server(("connect" + code), Server.getBroadAddress());
                    toggleButton.setEnabled(false);
                    break;
                case CLIENT_TAG:
                    client = new Client(code, lang);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    CompoundButton.OnCheckedChangeListener chekedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                lastListenMessage = "";
                partMessage = "";
                sendingMessage = "";
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                ActivityCompat.requestPermissions(SeansActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_PERMISSION);
            } else {
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.INVISIBLE);
                speechRecognizer.stopListening();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speechRecognizer.startListening(intent);
                } else {
                    Toast.makeText(SeansActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        super.onStop();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    private final Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            if (sendingMessage.length() > 0){
                synchronized (sendingMessage) {
                    client.sendTCP(sendingMessage);
                    sendingMessage = "";
                }
            }
            if (deviceIs.equals(CLIENT_TAG)) {
                recMessage = client.getRecMessage();
//            Log.d(LOG_TAG, "recMessage seans" + recMessage);
                if (recMessage != null) {
                    adapter.add(recMessage);
                    textToSpeech.speak(recMessage);

//                Log.d(LOG_TAG, "recMessage set to view " + recMessage);
                }
            }
            handler.postDelayed(timerTask, 300);
        }
    };

    private class listener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(SeansActivity.this, "You may say", Toast.LENGTH_LONG);
        }

        @Override
        public void onBeginningOfSpeech() {
            progressBar.setIndeterminate(false);
            progressBar.setMax(10);
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            progressBar.setProgress((int) rmsdB);
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            progressBar.setIndeterminate(true);
            toggleButton.setChecked(false);
        }

        @Override
        public void onError(int error) {
            String errorMessage = getErrorText(error);
            Log.d(LOG_TAG, "FAILED " + errorMessage);
            textViewListen.setText(errorMessage);
            toggleButton.setChecked(false);
        }

        @Override
        public void onResults(Bundle results) {
//            Log.i(LOG_TAG, "onResults");
            final ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = matches.get(0);
//            sendingMessage = "";
//            lastListenMessage = "";

            partMessage = text.substring(lastListenMessage.length());//part mess = word - lastlistenmessage
            sendingMessage = partMessage;
            textViewListen.setText(text);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            textViewListen.setText(word);

            partMessage = word.substring(lastListenMessage.length());//part mess = word - lastlistenmessage

            if (partMessage.contains(".") || partMessage.length() > 100) {
                sendingMessage = partMessage;
                lastListenMessage += partMessage;
                partMessage = "";
            }

            Log.i("TEST", "partial_results: " + word);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        public String getErrorText(int errorCode) {
            String message;
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "No speech input";
                    break;
                default:
                    message = "Didn't understand, please try again.";
                    break;
            }
            return message;
        }
    }

}
