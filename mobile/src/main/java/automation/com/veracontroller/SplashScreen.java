package automation.com.veracontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.async.FetchLocationDetailsTask;
import automation.com.veracontroller.util.RestClient;

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

        SharedPreferences sharedPref = SplashScreen.this.getPreferences(Context.MODE_PRIVATE);
        String localURL = sharedPref.getString("localUrl", null);

        if (localURL == null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    new FetchLocationDetailsTask(SplashScreen.this, true).execute();
                }
            }, SPLASH_DELAY);
        } else {
            String remoteUrl = sharedPref.getString("remoteUrl", null);
            String serialNumber = sharedPref.getString("serialNumber", null);
            RestClient.setRemoteURL(remoteUrl);
            RestClient.setLocalURL(localURL);
            //fetch user
            //fetch pwd
            //RestClient.updateCredentials(user, pwd, serialNumber);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, DeviceActivity.class));
                    SplashScreen.this.finish();
                }
            }, SPLASH_DELAY);
        }
    }
}
