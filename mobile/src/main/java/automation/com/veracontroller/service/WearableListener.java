package automation.com.veracontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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

import automation.com.veracontroller.R;
import automation.com.veracontroller.SplashScreen;
import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.enums.VeraType;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.pojo.session.Session;
import automation.com.veracontroller.pojo.session.SessionUI7;
import automation.com.veracontroller.util.RestClientUI7;
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
                            SystemClock.sleep(LIGHT_CONCURRENT_CALL_DELAY);
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
                    case WEARABLE_DEVICE_SCENE_EXECUTION:
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        try {
                            initializeClient();
                            executeScene(dataMap);
                            SystemClock.sleep(SCENE_CONCURRENT_CALL_DELAY);
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
                        Log.e("Incorrect Path", path + " not found.");
                        super.onDataChanged(dataEvents);
                        break;
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("SERVICE", "Message received: " + messageEvent.getPath());
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
        RestClientUI7.executeSceneCommand(scene.getSceneNum());
        saveSession();
    }

    private void toggleLightSwitch(DataMap dataMap) throws Exception {
        BinaryLight light = gson.fromJson(dataMap.getString(DataMapConstants.LIGHT), BinaryLight.class);
        Log.i("Toggle Attempt: ", light.getName()+", Future State: "+light.onOrOff(!light.isEnabled()));
        RestClientUI7.executeSwitchCommand(!light.isEnabled(), light.getDeviceNum());
        saveSession();
    }

    private void saveSession() {
        SharedPreferences.Editor editor = getSharedPreferences(
                PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(PreferenceConstants.SESSION_INFO, gson.toJson(RestClientUI7.getSession()));
        editor.commit();
    }

    private void initializeClient() throws Exception {
        SharedPreferences sharedPref = getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        VeraType veraType = VeraType.fromType(sharedPref.getString(PreferenceConstants.VERA_TYPE, null));

        if (veraType == null) {
            sendNotification("Smart Abode", "Please setup mobile app before accessing wearable app.");
            throw new NotSetupException("Not currently setup.");
        } else {
            Session session = RestClientUI7.getSession();
            if (session == null) {
                if (veraType == VeraType.VERA_UI5) {
                    session = gson.fromJson(sharedPref.getString(
                            PreferenceConstants.SESSION_INFO, null), Session.class);
                } else {
                    session = gson.fromJson(sharedPref.getString(
                            PreferenceConstants.SESSION_INFO, null), SessionUI7.class);
                }
                RestClientUI7.setSession(session);
            }
        }
    }

    private void fetchConfigurationDetails(DataPathEnum dataPathEnum) throws Exception {
        JSONObject result = RestClientUI7.fetchConfigurationDetails();
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
