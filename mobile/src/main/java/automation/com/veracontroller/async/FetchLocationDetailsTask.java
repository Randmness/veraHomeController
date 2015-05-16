package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.LoginActivity;
import automation.com.veracontroller.SplashScreen;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.util.RestClient;

public class FetchLocationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private boolean initialEntry;
    private String serialNumber;

    private ArrayList<String> userList = new ArrayList<>();

    public FetchLocationDetailsTask(Activity activity, boolean initialEntry) {
        this.activity = activity;
        this.initialEntry = initialEntry;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Fetching Data",
                "Retrieving automation setup...");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            processResult(RestClient.fetchLocationDetails());
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            return false;
        } finally {
            dialog.dismiss();
        }
        return true;
    }

    protected void processResult(JSONObject results) throws Exception {
        JSONObject unit = results.getJSONArray("units").getJSONObject(0);
        serialNumber = unit.getString("serialNumber");
        String localIP = unit.getString("ipAddress");
        String remoteUrl = unit.getString("active_server");

        updateConstants(serialNumber, localIP, remoteUrl);

        if (initialEntry) {
            JSONArray users = unit.getJSONArray("users");
            for (int index = 0; index < users.length(); index++) {
                userList.add(users.getString(index));
                Log.i("User", userList.get(index));
            }
        }
    }

    protected void updateConstants(String serialNumber, String localUrl, String remoteUrl) {
        String savedLocalUrl = "http://" + localUrl + ":3480/";
        String savedRemoteUrl = "http://" + remoteUrl + "/";

        RestClient.setLocalURL(savedLocalUrl);
        RestClient.setRemoteURL(savedRemoteUrl);


        SharedPreferences sharedPref = activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PreferenceConstants.SERIAL_NUMBER, serialNumber);
        editor.putString(PreferenceConstants.LOCAL_URL, savedLocalUrl);
        editor.putString(PreferenceConstants.REMOTE_URL, savedRemoteUrl);
        editor.commit();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            if (activity instanceof SplashScreen) {
                Intent intent = new Intent(activity, DeviceActivity.class);
                if (initialEntry) {
                    intent = new Intent(activity, LoginActivity.class);
                    intent.putStringArrayListExtra(IntentConstants.USER_LIST, userList);
                    intent.putExtra(IntentConstants.INITIAL_LOGIN, true);
                    intent.putExtra(IntentConstants.SERIAL_NUMBER, serialNumber);
                }
                activity.startActivity(intent);
                activity.finish();

            } else if (activity instanceof DeviceActivity) {
                if (initialEntry) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putStringArrayListExtra(IntentConstants.USER_LIST, userList);
                    intent.putExtra(IntentConstants.SERIAL_NUMBER, serialNumber);
                    activity.startActivity(intent);
                }
            }
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}