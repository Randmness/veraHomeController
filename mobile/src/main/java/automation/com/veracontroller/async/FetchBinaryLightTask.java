package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.HashMap;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.singleton.RoomData;
import automation.com.veracontroller.util.RestClient;

public class FetchBinaryLightTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;

    HashMap<Integer, Room> roomsMap = new HashMap<Integer, Room>();

    public FetchBinaryLightTask(Activity activity) {
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
            RoomData.resetMap(RestClient.fetchConfigurationDetails());
        } catch (Exception e) {
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
            activity.startActivity(intent);
            activity.finish();
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}