package com.example.android.fleetdemo;

/**
 * Created by Azuga on 27-02-2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private final int SPLASH_DURATION = 1000;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_splash);

        ((TextView)findViewById(R.id.splash_copyright_textt)).setText("©"+ getResources().getString(R.string.splash_copyright));

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intentToLoginScreen = new Intent(SplashActivity.this,
                        MainActivity.class);
                startActivity(intentToLoginScreen);
                SplashActivity.this.finish();

            }
        };

        mHandler.postDelayed(mRunnable, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

}