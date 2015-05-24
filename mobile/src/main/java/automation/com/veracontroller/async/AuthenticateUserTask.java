package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.enums.VeraType;
import automation.com.veracontroller.pojo.session.Session;
import automation.com.veracontroller.util.RestClientUI7;

public class AuthenticateUserTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private Gson gson = new Gson();
    private ProgressDialog dialog;
    private Session session;
    private String username;
    private String password;
    private VeraType veraType;
    private boolean initialLogin;

    public AuthenticateUserTask(Activity activity, String username, String password,
                                Session session, boolean initialLogin) {
        this.activity = activity;
        this.session = session;
        this.username = username;
        this.password = password;
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
            if (session.getSystemType() == VeraType.VERA_UI7) {
                session = RestClientUI7.initialSetup(username, password);
            } else {
                if (initialLogin) {
                    JSONObject unit = RestClientUI7.fetchLocationDetails();
                    session.setSerialNumber(unit.getString("serialNumber"));
                    session.setLocalUrl("http://" + unit.getString("ipAddress") + ":3480/");
                    session.setRemoteUrl("https://" + unit.getString("active_server") + "/");
                    RestClientUI7.setSession(session);

                }
                RestClientUI7.attemptAuthentication(username, password);
                session.setUserName(username);
                session.setPassword(password);

                StringBuilder remoteUrl = new StringBuilder(session.getRemoteUrl());
                remoteUrl.append(username);
                remoteUrl.append("/");
                remoteUrl.append(password);
                remoteUrl.append("/");
                remoteUrl.append(session.getSerialNumber());
                remoteUrl.append("/");
                session.setRemoteUrl(remoteUrl.toString());
            }
            SharedPreferences sharedPref =
                    activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (initialLogin) {
                editor.putString(PreferenceConstants.VERA_TYPE, session.getSystemType().toString());
            }
            editor.putString(PreferenceConstants.SESSION_INFO, gson.toJson(session));
            editor.commit();
            RestClientUI7.setSession(session);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            return false;
        } finally {
            dialog.dismiss();
        }
        return true;
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