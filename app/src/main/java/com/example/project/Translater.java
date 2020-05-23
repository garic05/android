package com.example.project;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Translater {
    public final String URL = "https://translate.yandex.net";
    public final String KEY = "trnsl.1.1.20200202T093336Z.7b711d7d053a1755.ad7638f5f440ce08c40df40da654a11b9ce23451";
    private final String lang;

    private Gson gson;
    private Retrofit retrofit;
    private ServiceAPI service;
    private Map<String, String> mapJson;

    public Translater(String lang){
        this.lang = new String(lang);
        Log.d("MY_TAG", "trnsl lang =" + lang);
        gson = new GsonBuilder().create();
        retrofit = new Retrofit
                .Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(URL)
                .build();

        service = retrofit.create(ServiceAPI.class);
    }

    public void translate(final Message message, final MessageQueue messageQueue) {
        mapJson = new HashMap<>();
        mapJson.put("key", KEY);
        mapJson.put("text", message.message);
        mapJson.put("lang", lang);
//        Log.d("MY_TAG", "start translate" + text);
//        returnTranslStr = "";
        if (message.message.length() > 0){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    service.translate(mapJson.get("key"),
                                        mapJson.get("text"),
                                        mapJson.get("lang"))
                            .enqueue(new Callback<TranslateData>() {
                        @Override
                        public void onResponse(Call<TranslateData> call, Response<TranslateData> response) {
                            TranslateData translateData = response.body();
                            messageQueue.offer(new Message(
                                                    message.IP,
                                                    translateData.getText().toString()
                            ));
//                            Log.d("MY_TAG", "end translate" + returnTranslStr);
                        }

                        @Override
                        public void onFailure(Call<TranslateData> call, Throwable t) {
                            messageQueue.offer(new Message(" " + message.IP, "error"));
                        }
                    });
                }
            }.start();

        }
    }

}