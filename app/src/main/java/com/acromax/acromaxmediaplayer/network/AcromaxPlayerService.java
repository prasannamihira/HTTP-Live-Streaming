package com.acromax.acromaxmediaplayer.network;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Mihira on 6/30/2018.
 */
public class AcromaxPlayerService {

    private AcromaxPlayerApi acromaxPlayerApi;

    private static AcromaxPlayerService acromaxPlayerService;

    /**
     * AcromaxPlayerService singleton
     * @return
     */
    public static AcromaxPlayerService getInstance(){
        if (acromaxPlayerService == null)
            acromaxPlayerService = new AcromaxPlayerService();
        return acromaxPlayerService;
    }

    /**
     * CCLeasingService constructor
     */
    private AcromaxPlayerService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.create();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(rxAdapter)
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        try {
            this.acromaxPlayerApi = retrofit.create(AcromaxPlayerApi.class);
        } catch (Exception ex) {
            Log.d("CCLeasingApi", ex.toString());
        }
    }

    /**
     * Initial stream request
     *
     * @return
     */
    public Observable<ResponseBody> initialStreamRequest(){
        return this.acromaxPlayerApi.initialStreamRequest();
    }

    /**
     * Audio stream request
     *
     * @return
     */
    public Observable<ResponseBody> audioStreamRequest(String bestAudioUri){
        return this.acromaxPlayerApi.audioStreamRequest(bestAudioUri);
    }

    /**
     * Download chunk file by length and offset
     *
     * @return
     */
    public Observable<ResponseBody> chunkDownloadRequest(String chunkUri, String range){
        return this.acromaxPlayerApi.chunkDownloadRequest(chunkUri, range);
    }


}
