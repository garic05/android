package com.example.project;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceAPI {
//    @FormUrlEncoded//записывает эти поля в тело запроса в таком же формате как они были бы записаны в URL строку при GET запросе
//    @POST("/api/v1.5/tr.json/translate")
    @GET("/api/v1.5/tr.json/translate")
    Call<TranslateData> translate(@Query("key") String key,
                                  @Query("text") String text,
                                  @Query("lang") String lang);
}