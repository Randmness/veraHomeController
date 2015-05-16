package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.fragments.BinaryLightFragment;
import automation.com.veracontroller.fragments.SceneFragment;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.service.PollingService;
import automation.com.veracontroller.util.RestClient;
import automation.com.veracontroller.util.RoomDataUtil;

public class FetchConfigurationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private ArrayList<BinaryLight> lightList;
    private ArrayList<Scene> sceneList;
    private List<Fragment> fragments;
    private boolean initialPull;
    private PollingService service;

    public FetchConfigurationDetailsTask(Activity activity, boolean initialPull) {
        this.activity = activity;
        this.initialPull = initialPull;
    }

    public FetchConfigurationDetailsTask(boolean initialPull, List<Fragment> fragments, PollingService service) {
        this.initialPull = initialPull;
        this.fragments = fragments;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        if (service == null) {
            dialog = ProgressDialog.show(activity, "Fetch Configuration Details",
                    "Attempting to fetch configuration...");
        }
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
            if (dialog != null) {
                dialog.dismiss();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (initialPull) {
                Intent intent = new Intent(activity, DeviceActivity.class);
                intent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lightList);
                intent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, sceneList);
                activity.startActivity(intent);
                activity.finish();
            } else {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof BinaryLightFragment) {
                        ((BinaryLightFragment) fragment).pollingUpdate(lightList);
                    } else if (fragment instanceof SceneFragment) {
                        ((SceneFragment) fragment).pollingUpdate(sceneList);
                    }
                }
                service.callJobFinished();
            }
        } else {
            Toast.makeText(activity, "Failed to retrieve configuration.", Toast.LENGTH_LONG).show();
        }
    }
}