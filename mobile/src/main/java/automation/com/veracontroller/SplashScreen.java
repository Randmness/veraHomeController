package automation.com.veracontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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

        SharedPreferences sharedPref = SplashScreen.this.getSharedPreferences("PREF", Context.MODE_PRIVATE);
        String localURL = sharedPref.getString("localUrl", null);

        if (localURL == null || true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder webDialog = new AlertDialog.Builder(SplashScreen.this);
                    webDialog.setMessage("More information?");
                    webDialog.setCancelable(true);
                    webDialog.setPositiveButton("Local Only",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new FetchLocationDetailsTask(SplashScreen.this, false).execute();
                                }
                            });
                    webDialog.setNegativeButton("Remote",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new FetchLocationDetailsTask(SplashScreen.this, true).execute();
                                }
                            });

                    AlertDialog alertWeb = webDialog.create();
                    alertWeb.show();
                }
            }, SPLASH_DELAY);
        } else {
            boolean leverageRemote = sharedPref.getBoolean("leverageRemote", false);
            if (leverageRemote) {
                String remoteUrl = sharedPref.getString("remoteUrl", null);
                String serialNumber = sharedPref.getString("serialNumber", null);
                String username = sharedPref.getString("username", null);
                String password = sharedPref.getString("password", null);
                RestClient.setRemoteURL(remoteUrl);
                RestClient.updateCredentials(username, password, serialNumber);
                RestClient.setLeverageRemote(leverageRemote);
            } else {
                RestClient.setLocalURL(localURL);
            }

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
