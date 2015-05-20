package automation.com.veracontroller.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.util.support.DeviceTypeEnum;
import automation.com.veracontroller.util.support.ServiceTypeEnum;

/**
 * Created by root on 2/22/15.
 */
abstract public class RoomDataUtil {

    public static List<BinaryLight> getLights(JSONObject results) throws JSONException {

        HashMap<Integer, String> rooms = new HashMap<>();
        JSONArray roomList = results.getJSONArray("rooms");
        for (int index = 0; index < roomList.length(); index++) {
            JSONObject room = roomList.getJSONObject(index);
            int roomID = room.getInt("id");
            String roomName = room.getString("name");
            rooms.put(roomID, roomName);
        }

        List<BinaryLight> lights = new ArrayList<>();
        JSONArray devices = results.getJSONArray("devices");
        for (int index = 0; index < devices.length(); index++) {
            JSONObject device = devices.getJSONObject(index);
            String deviceType = device.getString("device_type");
            if (deviceType != null && deviceType.equals(DeviceTypeEnum.BINARY_LIGHT.toString())) {
                int deviceID = device.getInt("id");
                int roomID = device.getInt("room");
                String name = device.getString("name");

                String roomName = rooms.get(roomID);
                if (roomID == 0) {
                    roomName = "No Room";
                }

                BinaryLight light = new BinaryLight(deviceID, name, roomName);

                JSONArray states = device.getJSONArray("states");
                for (int stateIndex = 0; stateIndex < states.length(); stateIndex++) {
                    JSONObject state = states.getJSONObject(stateIndex);
                    String service = state.getString("service");
                    if (service != null && service.equals(ServiceTypeEnum.SWITCH_LIGHT.toString()) &&
                            state.getString("variable").equals("Status") && state.getString("value").equals("1")) {
                        light.setState(true);
                        break;
                    }
                }

                lights.add(light);
            }
        }

        Collections.sort(lights, BinaryLight.Comparators.NAME);
        return lights;
    }

    public static List<Scene> getScenes(JSONObject results) throws JSONException {
        HashMap<Integer, String> rooms = new HashMap<>();
        JSONArray roomList = results.getJSONArray("rooms");
        for (int index = 0; index < roomList.length(); index++) {
            JSONObject room = roomList.getJSONObject(index);
            int roomID = room.getInt("id");
            String roomName = room.getString("name");
            rooms.put(roomID, roomName);
        }

        List<Scene> scenes = new ArrayList<>();
        JSONArray sceneList = results.getJSONArray("scenes");
        for (int index = 0; index < sceneList.length(); index++) {
            JSONObject scene = sceneList.getJSONObject(index);
            if (!scene.has("notification_only")) {
                int sceneNum = scene.getInt("id");
                String sceneName = scene.getString("name");
                int roomID = scene.getInt("room");
                String roomName = rooms.get(roomID);
                if (roomID == 0) {
                    roomName = "No Room";
                }
                scenes.add(new Scene(sceneNum, sceneName, roomName));
            }
        }

        Collections.sort(scenes, Scene.Comparators.NAME);
        return scenes;
    }
}
