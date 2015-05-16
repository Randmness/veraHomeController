package automation.com.veracontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.R;

/**
 * Created by mrand on 5/15/15.
 */
public class WearableListener extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private Gson gson = new Gson();

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.i("onDataChanged", "data received");
        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                sendLocalNotification(dataMap);
                Log.v("myTag", "DataMap received on watch: " + dataMap);
            }
        }
    }

    private void sendLocalNotification(DataMap dataMap) {
        int notificationId = 001;

        ArrayList<BinaryLight> lightList = gson.fromJson(dataMap.getString("LIGHTS"),
                new TypeToken<ArrayList<BinaryLight>>() {
                }.getType());
        ArrayList<Scene> sceneList = gson.fromJson(dataMap.getString("SCENE"),
                new TypeToken<ArrayList<BinaryLight>>() {
                }.getType());

        Log.i("TRANSFORMATION WORKED", lightList.size()+"");
        Log.i("TRANSFORMATION WORKED", sceneList.size()+"");

        // Create a pending intent that starts this wearable app
        Intent startIntent = new Intent(this, DeviceActivity.class).setAction(Intent.ACTION_MAIN);
        // Add extra data for app startup or initialization, if available
        startIntent.putParcelableArrayListExtra("LIGHTS", lightList);
        startIntent.putParcelableArrayListExtra("SCENES", sceneList);
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notify = new NotificationCompat.Builder(this)
                .setContentTitle("Title")
                .setContentText("Open app")
                .setSmallIcon(R.drawable.ic_full_sad)
                .setAutoCancel(true)
                .setContentIntent(startPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notify);
    }
}
