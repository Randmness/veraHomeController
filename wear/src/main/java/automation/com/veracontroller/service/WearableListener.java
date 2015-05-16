package automation.com.veracontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.R;
import automation.com.veracontroller.SplashActivity;

/**
 * Created by mrand on 5/15/15.
 */
public class WearableListener extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

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

        // Create a pending intent that starts this wearable app
        Intent startIntent = new Intent(this, DeviceActivity.class).setAction(Intent.ACTION_MAIN);
        // Add extra data for app startup or initialization, if available
        startIntent.putExtra("extra", dataMap.getString("extra"));
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notify = new NotificationCompat.Builder(this)
                //.setContentTitle(dataMap.getString("title"))
                .setContentTitle("Title")
                .setContentText("Open app")
                //.setContentText(dataMap.getString("body"))
                .setSmallIcon(R.drawable.ic_full_sad)
                .setAutoCancel(true)
                .setContentIntent(startPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notify);
    }
}
