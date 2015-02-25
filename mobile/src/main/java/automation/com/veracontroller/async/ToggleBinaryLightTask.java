package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import android.widget.ListAdapter;
import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.R;
import automation.com.veracontroller.fragments.BinaryLightFragment;
import automation.com.veracontroller.pojo.BinaryLight;
import android.support.v4.app.FragmentTransaction;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.singleton.RoomData;
import automation.com.veracontroller.util.RestClient;

public class ToggleBinaryLightTask extends AsyncTask<Void, Void, Boolean> {
    private BinaryLight light;
    private ProgressDialog dialog;
    private Context context;

    public ToggleBinaryLightTask(Context context, BinaryLight light) {
        this.light = light;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "Fetching Data",
                "Turning "+light.getName()+" "+ light.onOrOff(!light.isEnabled()));
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            RestClient.executeSwitchCommand(!light.isEnabled(), light.getDeviceNum());
            RoomData.resetMap(RestClient.fetchConfigurationDetails());
        } catch (Exception e) {
            Log.e("Failure", "Failed to execute command.");
            return false;
        } finally {
            dialog.dismiss();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();;
        }
    }
}