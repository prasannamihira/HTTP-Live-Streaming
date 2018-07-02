package com.acromax.acromaxmediaplayer.view.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

/**
 * Created by Mihira on 6/30/2018.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Check network connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        if (netInfo != null) {
            if (netInfo.isConnected()) {
                // Internet Available
                Log.e("Connectivity >> ", netInfo.getDetailedState().toString());

                if(!netInfo.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)) {
                    return false;

                } else {
                    return true;
                }
            }else {
                //No internet
                return false;
            }
        } else {
            return false;
        }
    }

}
