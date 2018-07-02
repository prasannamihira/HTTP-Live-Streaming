package com.acromax.acromaxmediaplayer.network;

import android.net.Uri;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Mihira on 6/30/2018.
 */
public interface AcromaxPlayerApi {

    @Headers({
            "Content-Type: application/json",
            "Accept-Charset: utf-8"
    })

    /**
     * Initial stream data
     */
    @GET("hls_index.m3u8")
    Observable<ResponseBody> initialStreamRequest();

    /**
     * Best audio stream data
     */
    @GET
    Observable<ResponseBody> audioStreamRequest(@Url String bestAudioUri);

    /**
     * Download chunk using length and offset
     */

    @GET
    Observable<ResponseBody> chunkDownloadRequest(@Url String chunkUri, @Header("Range") String range);

}
