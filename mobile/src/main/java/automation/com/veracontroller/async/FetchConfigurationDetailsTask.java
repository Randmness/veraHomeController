package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.SplashScreen;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.util.RestClient;
import automation.com.veracontroller.util.RoomDataUtil;

public class FetchConfigurationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private ArrayList<BinaryLight> lightList;
    private ArrayList<Scene> sceneList;

    public FetchConfigurationDetailsTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Fetch Configuration Details",
                "Attempting to fetch configuration...");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            JSONObject response = RestClient.fetchConfigurationDetails();
            lightList = (ArrayList) RoomDataUtil.getLights(response);
            sceneList = (ArrayList) RoomDataUtil.getScenes(response);
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
            Intent intent = new Intent(activity, DeviceActivity.class);
            intent.putParcelableArrayListExtra(DeviceActivity.LIGHT_LIST, lightList);
            intent.putParcelableArrayListExtra(DeviceActivity.SCENE_LIST, sceneList);
            activity.startActivity(intent);
            activity.finish();
        } else {
            Toast.makeText(activity, "Failed to authenticate.", Toast.LENGTH_LONG).show();
        }
    }
}