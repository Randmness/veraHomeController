package automation.com.veracontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.google.gson.Gson;

import automation.com.veracontroller.async.FetchConfigurationDetailsTask;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.enums.VeraType;
import automation.com.veracontroller.pojo.session.Session;
import automation.com.veracontroller.pojo.session.SessionUI7;
import automation.com.veracontroller.util.RestClientUI7;

public class SplashScreen extends Activity {

    private static final int SPLASH_DELAY = 3000;
    private ImageView splash;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splash = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences sharedPref = SplashScreen.this.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        VeraType veraType = VeraType.fromType(sharedPref.getString(PreferenceConstants.VERA_TYPE, null));

        if (veraType == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.recoveryRemote);
            builder.setCancelable(false)
                    .setPositiveButton("Vera UI7", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                            intent.putExtra(IntentConstants.INITIAL_LOGIN, true);
                            intent.putExtra(IntentConstants.VERA_TYPE, VeraType.VERA_UI7.toString());
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNeutralButton("Vera UI5", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                            intent.putExtra(IntentConstants.INITIAL_LOGIN, true);
                            intent.putExtra(IntentConstants.VERA_TYPE, VeraType.VERA_UI5.toString());
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel Setup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SplashScreen.this.finish();
                        }
                    });
            builder.create().show();
        } else {
            Session session = null;
            if (veraType == VeraType.VERA_UI5) {
                session = gson.fromJson(sharedPref.getString(
                        PreferenceConstants.SESSION_INFO,null), Session.class);
            } else {
                session = gson.fromJson(sharedPref.getString(
                        PreferenceConstants.SESSION_INFO,null), SessionUI7.class);
            }
            RestClientUI7.setSession(session);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new FetchConfigurationDetailsTask(SplashScreen.this, true).execute();
                }
            }, SPLASH_DELAY);
        }
    }
}
