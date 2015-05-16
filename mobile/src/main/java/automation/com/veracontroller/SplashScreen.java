package automation.com.veracontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import automation.com.veracontroller.async.FetchConfigurationDetailsTask;
import automation.com.veracontroller.async.FetchLocationDetailsTask;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.util.RestClient;

public class SplashScreen extends Activity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences sharedPref = SplashScreen.this.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        String localURL = sharedPref.getString(PreferenceConstants.LOCAL_URL, null);

        if (localURL == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder webDialog = new AlertDialog.Builder(SplashScreen.this);
                    webDialog.setMessage("More information?");
                    webDialog.setCancelable(false);
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
            boolean leverageRemote = sharedPref.getBoolean(PreferenceConstants.LEVERAGE_REMOTE, false);
            if (leverageRemote) {
                String remoteUrl = sharedPref.getString(PreferenceConstants.REMOTE_URL, null);
                String serialNumber = sharedPref.getString(PreferenceConstants.SERIAL_NUMBER, null);
                String username = sharedPref.getString(PreferenceConstants.USER_NAME, null);
                String password = sharedPref.getString(PreferenceConstants.PASSWORD, null);
                RestClient.setRemoteURL(remoteUrl);
                RestClient.updateCredentials(username, password, serialNumber);
                RestClient.setLeverageRemote(leverageRemote);
            }
            RestClient.setLocalURL(localURL);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new FetchConfigurationDetailsTask(SplashScreen.this, true).execute();
                }
            }, SPLASH_DELAY);
        }
    }
}
