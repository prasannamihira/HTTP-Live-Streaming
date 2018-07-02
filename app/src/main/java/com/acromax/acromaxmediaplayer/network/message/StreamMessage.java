package com.acromax.acromaxmediaplayer.network.message;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * Created by Mihira on 6/30/2018.
 */
public interface StreamMessage {

    /**
     * @param responseStream
     */
    void onSuccessStream(ResponseBody responseStream);

    /**
     * @param error
     */
    void onFailureStream(Throwable error);
}
