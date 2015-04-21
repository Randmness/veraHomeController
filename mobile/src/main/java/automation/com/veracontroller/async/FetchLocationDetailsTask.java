package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.singleton.RoomData;
import automation.com.veracontroller.util.RestClient;

public class FetchLocationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;

    HashMap<Integer, Room> roomsMap = new HashMap<Integer, Room>();

    public FetchLocationDetailsTask(Activity activity) {
        this.activity = activity;
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

        JSONArray users = unit.getJSONArray("users");
        List<String> userList = new ArrayList<String>();
        for(int index = 0; index < users.length(); index++) {
            JSONObject user = users.getJSONObject(index);
            userList.add(user.toString());
            Log.i("User", user.toString());
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (!result) {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}