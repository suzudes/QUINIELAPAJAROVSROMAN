package com.example.quinielapajarovsroman;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://quinielapajarovsroman.onrender.com/"; 
    private static Retrofit retrofit = null;
    private static PersistentCookieJar cookieJar = null;

    public static ApiService getService(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            cookieJar = new PersistentCookieJar(context);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .cookieJar(cookieJar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static PersistentCookieJar getCookieJar(Context context) {
        if (cookieJar == null) cookieJar = new PersistentCookieJar(context);
        return cookieJar;
    }
}
