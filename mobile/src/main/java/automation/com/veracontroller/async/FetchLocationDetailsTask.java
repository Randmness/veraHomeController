package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.util.RestClient;

public class FetchLocationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private boolean initialEntry;

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

    protected void processResult (JSONObject results) throws Exception {
        JSONObject unit = results.getJSONArray("units").getJSONObject(0);
        String serialNumber = unit.getString("serialNumber");
        String localIP = unit.getString("ipAddress");
        String remoteUrl = unit.getString("active_server");
        updateConstants(serialNumber, localIP, remoteUrl);

        JSONArray users = unit.getJSONArray("users");
        List<String> userList = new ArrayList<String>();
        for(int index = 0; index < users.length(); index++) {
            userList.add(users.getString(index));
            Log.i("User", userList.get(index));
        }
    }

    protected void updateConstants(String serialNumber, String localUrl, String remoteUrl) {
        String savedLocalUrl = "http://"+localUrl+":3480/";
        String savedRemoteUrl = "http://"+remoteUrl+"/";

        RestClient.setLocalURL(savedLocalUrl);
        RestClient.setRemoteURL(savedRemoteUrl);


        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("serialNumber", serialNumber);
        editor.putString("localUrl", savedLocalUrl);
        editor.putString("remoteUrl", savedRemoteUrl);
        editor.commit();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            if (initialEntry) {
                activity.startActivity(new Intent(activity, DeviceActivity.class));
                activity.finish();
            }
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}