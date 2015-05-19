package automation.com.veracontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.enums.DataPathEnum;
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

public class WearableListener extends WearableListenerService {
    private Gson gson = new Gson();

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i("Wearable Listener", "onDataChanged invoked");
        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                Log.i("Wearable Listener", "Data received on path: "+path);
                switch (DataPathEnum.fromPath(path)) {
                    case WEARABLE_DEVICE_ACTIVITY_LAUNCH:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        sendLocalNotification(dataMap);
                        break;
                    case WEARABLE_CONFIG_DATA_RESPONSE:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        broadcastConfigReceiver(dataMap, DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE);
                        break;
                    case WEARABLE_SPLASH_DATA_RESPONSE:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        broadcastSplashReceiver(dataMap, DataPathEnum.WEARABLE_SPLASH_DATA_RESPONSE);
                        break;
                    case WEARABLE_SPLASH_DATA_ERROR:
                        broadcastErrorResponse(DataPathEnum.WEARABLE_SPLASH_DATA_ERROR);
                        break;
                    case WEARABLE_SPLASH_ERROR_NOT_SETUP_RESPONSE:
                        broadcastErrorResponse(DataPathEnum.WEARABLE_SPLASH_ERROR_NOT_SETUP_RESPONSE);
                        break;
                    case WEARABLE_CONFIG_DATA_ERROR:
                        broadcastErrorResponse(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR);
                        break;
                    default:
                        Log.e("Incorrect Path", path + " not found.");
                        break;
                }
            }
        }
    }

    private void broadcastSplashReceiver(DataMap dataMap, DataPathEnum dataPathEnum) {
        ArrayList<BinaryLight> lightList = gson.fromJson(dataMap.getString(DataMapConstants.LIGHT_LIST),
                new TypeToken<ArrayList<BinaryLight>>() {
                }.getType());
        ArrayList<Scene> sceneList = gson.fromJson(dataMap.getString(DataMapConstants.SCENE_LIST),
                new TypeToken<ArrayList<Scene>>() {
                }.getType());

        Intent messageIntent = new Intent();
        messageIntent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lightList);
        messageIntent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, sceneList);
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra(IntentConstants.DATA_PATH, dataPathEnum.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }

    private void broadcastConfigReceiver(DataMap dataMap, DataPathEnum dataPathEnum) {
        ArrayList<BinaryLight> lightList = gson.fromJson(dataMap.getString(DataMapConstants.LIGHT_LIST),
                new TypeToken<ArrayList<BinaryLight>>() {
                }.getType());
        ArrayList<Scene> sceneList = gson.fromJson(dataMap.getString(DataMapConstants.SCENE_LIST),
                new TypeToken<ArrayList<Scene>>() {
                }.getType());

        Intent messageIntent = new Intent();
        messageIntent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lightList);
        messageIntent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, sceneList);
        messageIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        messageIntent.putExtra(IntentConstants.DATA_PATH, dataPathEnum.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }

    private void broadcastErrorResponse(DataPathEnum dataPathEnum) {
        Intent messageIntent = new Intent();
        switch (dataPathEnum) {
            case WEARABLE_SPLASH_DATA_ERROR:
                messageIntent.setAction(Intent.ACTION_SEND);
                break;
            case WEARABLE_SPLASH_ERROR_NOT_SETUP_RESPONSE:
                messageIntent.setAction(Intent.ACTION_SEND);
                break;
            case WEARABLE_CONFIG_DATA_ERROR:
                messageIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                break;
        }

        messageIntent.putExtra(IntentConstants.DATA_PATH, dataPathEnum.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }

    private void sendLocalNotification(DataMap dataMap) {
        int notificationId = 001;

        ArrayList<BinaryLight> lightList = gson.fromJson(dataMap.getString(DataMapConstants.LIGHT_LIST),
                new TypeToken<ArrayList<BinaryLight>>() {
                }.getType());
        ArrayList<Scene> sceneList = gson.fromJson(dataMap.getString(DataMapConstants.SCENE_LIST),
                new TypeToken<ArrayList<Scene>>() {
                }.getType());

        // Create a pending intent that starts this wearable app
        Intent startIntent = new Intent(this, DeviceActivity.class).setAction(Intent.ACTION_MAIN);
        startIntent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lightList);
        startIntent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, sceneList);
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notify = new NotificationCompat.Builder(this)
                .setContentTitle("Smart Abode")
                .setContentText("Control Vera via your wearable.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(startPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notify);
    }
}
