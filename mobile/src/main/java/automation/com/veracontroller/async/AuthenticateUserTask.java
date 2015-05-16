package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.util.RestClient;

public class AuthenticateUserTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private String username;
    private String password;
    private String serial;
    private boolean initialLogin;

    public AuthenticateUserTask(Activity activity, String username, String password, String serial, boolean initialLogin) {
        this.activity = activity;
        this.username = username;
        this.password = password;
        this.serial = serial;
        this.initialLogin = initialLogin;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Authentication",
                "Attempting to authenticate user...");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            RestClient.attemptAuthentication(username, password, serial);
            updateConstants();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            return false;
        } finally {
            dialog.dismiss();
        }
        return true;
    }

    protected void updateConstants() {
        SharedPreferences sharedPref = activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PreferenceConstants.USER_NAME, username);
        editor.putString(PreferenceConstants.PASSWORD, password);
        editor.putBoolean(PreferenceConstants.LEVERAGE_REMOTE, true);
        editor.commit();

        RestClient.setRemoteURL(sharedPref.getString(PreferenceConstants.REMOTE_URL, null));
        Log.i("updateCredentials", username + " " + password);
        RestClient.updateCredentials(username, password, serial);
        RestClient.setLeverageRemote(true);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (initialLogin) {
                new FetchConfigurationDetailsTask(activity, true).execute();
            } else {
                activity.finish();
            }
        } else {
            Toast.makeText(activity, "Failed to authenticate.", Toast.LENGTH_LONG).show();
        }
    }
}