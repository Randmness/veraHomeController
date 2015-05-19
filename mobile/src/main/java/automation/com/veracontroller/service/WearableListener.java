package automation.com.veracontroller.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.R;
import automation.com.veracontroller.SplashScreen;
import automation.com.veracontroller.async.FetchLocationDetailsTask;
import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.util.RestClient;
import automation.com.veracontroller.util.RoomDataUtil;

/**
 * Created by mrand on 5/15/15.
 */
public class WearableListener extends WearableListenerService{

    private static final int LIGHT_CONCURRENT_CALL_DELAY = 500;
    private static final int SCENE_CONCURRENT_CALL_DELAY = 2500;

    private Gson gson = new Gson();
    private GoogleApiClient googleClient;

    @Override
    public void onCreate() {

        super.onCreate();
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleClient.connect();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                Log.i("Mobile Listener", "Data received on path: "+path);
                switch (DataPathEnum.fromPath(path)) {
                    case WEARABLE_DEVICE_LIGHT_TOGGLE:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        try {
                            initializeClient();
                            toggleLightSwitch(dataMap);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        fetchConfigurationDetails(DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE);
                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                        DataMap dataMapResponse = new DataMap();
                                        dataMapResponse.putString(DataMapConstants.ERROR, "Failed");
                                        dataMapResponse.putString("UUID", UUID.randomUUID().toString());
                                        new DataLayerThread(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR.toString(),
                                                dataMapResponse, googleClient).start();
                                    }
                                }
                            }, LIGHT_CONCURRENT_CALL_DELAY);
                        } catch (Exception e) {
                            Log.e("Exception", e.getMessage());
                            DataMap dataMapResponse = new DataMap();
                            dataMapResponse.putString(DataMapConstants.ERROR, "Failed");
                            dataMapResponse.putString("UUID", UUID.randomUUID().toString());
                            new DataLayerThread(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR.toString(),
                                    dataMapResponse, googleClient).start();
                        }
                        break;
                    case WEARABLE_DEVICE_SCENE_EXECUTION:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        try {
                            initializeClient();
                            executeScene(dataMap);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        fetchConfigurationDetails(DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE);
                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                        DataMap dataMapResponse = new DataMap();
                                        dataMapResponse.putString(DataMapConstants.ERROR, "Failed");
                                        dataMapResponse.putString("UUID", UUID.randomUUID().toString());
                                        new DataLayerThread(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR.toString(),
                                                dataMapResponse, googleClient).start();
                                    }
                                }
                            }, SCENE_CONCURRENT_CALL_DELAY);
                        } catch (Exception e) {
                            Log.e("Exception", e.getMessage());
                            DataMap dataMapResponse = new DataMap();
                            dataMapResponse.putString(DataMapConstants.ERROR, "Failed");
                            dataMapResponse.putString("UUID", UUID.randomUUID().toString());
                            new DataLayerThread(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR.toString(),
                                    dataMapResponse, googleClient).start();
                        }
                        break;
                    default:
                        Log.e("Incorrect Path", path + " not found.");
                        super.onDataChanged(dataEvents);
                        break;
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("SERVICE", "Message received: "+messageEvent.getPath());
        switch (DataPathEnum.fromPath(messageEvent.getPath())) {
            case WEARABLE_SPLASH_DATA_REQUEST:
                try {
                    initializeClient();
                    fetchConfigurationDetails(DataPathEnum.WEARABLE_SPLASH_DATA_RESPONSE);
                } catch (NotSetupException e) {
                    Log.e("Exception", e.getMessage());
                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.ERROR, "Failed");
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_SPLASH_ERROR_NOT_SETUP_RESPONSE.toString(),
                            dataMap, googleClient).start();
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());
                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.ERROR, "Failed");
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_SPLASH_DATA_ERROR.toString(),
                            dataMap, googleClient).start();
                }
                break;
            case WEARABLE_CONFIG_DATA_REQUEST:
                try {
                    initializeClient();
                    fetchConfigurationDetails(DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE);
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());
                    DataMap dataMapResponse = new DataMap();
                    dataMapResponse.putString(DataMapConstants.ERROR, "Failed");
                    dataMapResponse.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_CONFIG_DATA_ERROR.toString(),
                            dataMapResponse, googleClient).start();
                }
                break;
            default:
                super.onMessageReceived(messageEvent);
        }
    }

    private void executeScene(DataMap dataMap) throws Exception {
        initializeClient();
        Scene scene =  gson.fromJson(dataMap.getString(DataMapConstants.SCENE), Scene.class);
        Log.i("Scene Execution", "Attempting to execute scene:" + scene.getSceneName());
        RestClient.executeSceneCommand(scene.getSceneNum());
    }

    private void toggleLightSwitch(DataMap dataMap) throws Exception {
        BinaryLight light = gson.fromJson(dataMap.getString(DataMapConstants.LIGHT), BinaryLight.class);
        Log.i("Toggle Attempt: ", light.getName()+", Future State: "+light.onOrOff(!light.isEnabled()));
        RestClient.executeSwitchCommand(!light.isEnabled(), light.getDeviceNum());
    }

    private void initializeClient() throws Exception {
        SharedPreferences sharedPref = getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        String localUrl = sharedPref.getString(PreferenceConstants.LOCAL_URL, null);

        if (localUrl == null) {
            sendNotification("Smart Abode", "Please set up before accessing wearable app.");
            throw new NotSetupException("Not currently setup.");
        } else {
            //setup, but not active
            if (RestClient.getLocalUrl() == null) {
                boolean leverageRemote = sharedPref.getBoolean(PreferenceConstants.LEVERAGE_REMOTE, false);
                RestClient.setLocalURL(localUrl);
                if (leverageRemote) {
                    String username = sharedPref.getString(PreferenceConstants.USER_NAME, null);
                    String password = sharedPref.getString(PreferenceConstants.PASSWORD, null);
                    String serial = sharedPref.getString(PreferenceConstants.SERIAL_NUMBER, null);
                    String remote = sharedPref.getString(PreferenceConstants.REMOTE_URL, null);
                    RestClient.setLeverageRemote(true);
                    RestClient.setRemoteURL(remote);
                    RestClient.updateCredentials(username, password, serial);
                }
            }
        }
    }

    private void fetchConfigurationDetails(DataPathEnum dataPathEnum) throws Exception {
        JSONObject result = RestClient.fetchConfigurationDetails();
        List<BinaryLight> lights = RoomDataUtil.getLights(result);
        List<Scene> scenes = RoomDataUtil.getScenes(result);

        DataMap dataMap = new DataMap();
        String lightList = gson.toJson(lights, new TypeToken<ArrayList<BinaryLight>>(){}.getType());
        String sceneList = gson.toJson(scenes, new TypeToken<ArrayList<Scene>>(){}.getType());
        dataMap.putString(DataMapConstants.LIGHT_LIST, lightList);
        dataMap.putString(DataMapConstants.SCENE_LIST, sceneList);
        dataMap.putString("UUID", UUID.randomUUID().toString());
        Log.i("Wearable listener", "Attempting to send data to : " + dataPathEnum.toString());
        new DataLayerThread(dataPathEnum.toString(), dataMap, googleClient).start();
    }

    private void sendNotification(String title, String message) {
        Intent startIntent = new Intent(getApplicationContext(), SplashScreen.class);
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notify = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.splash_screen))
                .setAutoCancel(true)
                .setContentIntent(startPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(001, notify);
    }
}
