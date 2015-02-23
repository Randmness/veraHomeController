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

import automation.com.veracontroller.MainActivity;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.pojo.support.DeviceTypeEnum;
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
                "Retrieving setup from ");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            JSONObject results = RestClient.fetchConfigurationDetails();

            JSONArray rooms = results.getJSONArray("rooms");
            for (int index = 0; index < rooms.length(); index++) {
                JSONObject room = rooms.getJSONObject(index);
                int roomID = room.getInt("id");
                String roomName = room.getString("name");
                roomsMap.put(roomID, new Room(roomID, roomName));

                Log.i("ROOMS: ",roomID+"-"+roomName);
            }

            JSONArray devices = results.getJSONArray("devices");
            for (int index = 0; index < devices.length(); index++) {
                JSONObject device = devices.getJSONObject(index);
                String deviceType = device.getString("device_type");

                if (deviceType != null && deviceType.equals(DeviceTypeEnum.BINARY_LIGHT)) {
                    int deviceID = device.getInt("id");
                    int roomID = device.getInt("room");

                    Room room = roomsMap.get(roomID);
                    room.addBinaryLight(new BinaryLight(deviceID));

                    Log.i("DEVICE: ", deviceID+"-"+roomID);
                }
            }

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
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("ROOMS", roomsMap);
            activity.startActivity(intent);
            activity.finish();
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}