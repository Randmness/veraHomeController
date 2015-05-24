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
import automation.com.veracontroller.pojo.session.SessionUI7;
import automation.com.veracontroller.util.RestClientUI7;

public class FetchLocationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private Session session;
    private Gson gson = new Gson();

    public FetchLocationDetailsTask(Activity activity, Session session) {
        this.activity = activity;
        this.session = session;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Fetching Data",
                "Retrieving automation setup...");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            if (session.getSystemType() == VeraType.VERA_UI7) {
                SessionUI7 sessionUI7 = (SessionUI7) session;
                SessionUI7 newInfo = RestClientUI7.initialSetup(session.getUserName(), session.getPassword());
                sessionUI7.setLocalUrl(newInfo.getLocalUrl());
                sessionUI7.setRemoteUrl(newInfo.getRemoteUrl());
                sessionUI7.setServerRelay(newInfo.getServerRelay());
                sessionUI7.setSessionToken(newInfo.getSessionToken());
            } else {
                JSONObject unit = RestClientUI7.fetchLocationDetails();
                session.setSerialNumber(unit.getString("serialNumber"));
                session.setLocalUrl("http://" + unit.getString("ipAddress") + ":3480/");
                session.setRemoteUrl("https://" + unit.getString("active_server") + "/");
            }
            SharedPreferences sharedPref =
                    activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
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
            Toast.makeText(activity, "Successfully updated the location details.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}