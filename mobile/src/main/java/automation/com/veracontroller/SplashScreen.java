package automation.com.veracontroller;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.async.FetchLocationDetailsTask;

public class SplashScreen extends Activity {

    private static final int SPLASH_DELAY = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                new FetchLocationDetailsTask(SplashScreen.this).execute();
            }
        }, SPLASH_DELAY);
/*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new FetchBinaryLightTask(SplashScreen.this).execute();
            }
        }, SPLASH_DELAY);*/
    }
}
