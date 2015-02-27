package automation.com.veracontroller.singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.pojo.support.DeviceTypeEnum;
import automation.com.veracontroller.pojo.support.ServiceTypeEnum;

/**
 * Created by root on 2/22/15.
 */
abstract public class RoomData {

    //Rooms and Lights
    private static HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();
    private static List<String> lightIDs = new ArrayList<String>();
    private static HashMap<String, BinaryLight> lightMap = new HashMap<String, BinaryLight>();

    //Scenes
    private static List<Scene> scenes = new ArrayList<Scene>();
    private static List<String> sceneNames = new ArrayList<String>();

    public static void resetMap(JSONObject results) throws JSONException {
        setupScenes(results);

        rooms.clear();
        lightIDs.clear();
        lightMap.clear();

        JSONArray roomList = results.getJSONArray("rooms");
        for (int index = 0; index < roomList.length(); index++) {
            JSONObject room = roomList.getJSONObject(index);
            int roomID = room.getInt("id");
            String roomName = room.getString("name");
            rooms.put(roomID, new Room(roomID, roomName));
        }

        JSONArray devices = results.getJSONArray("devices");
        for (int index = 0; index < devices.length(); index++) {
            JSONObject device = devices.getJSONObject(index);
            String deviceType = device.getString("device_type");
            if (deviceType != null && deviceType.equals(DeviceTypeEnum.BINARY_LIGHT.toString())) {
                int deviceID = device.getInt("id");
                int roomID = device.getInt("room");
                String name = device.getString("name");

                BinaryLight light = new BinaryLight(deviceID, name, roomID);
                lightIDs.add(Integer.toString(light.getDeviceNum()));
                lightMap.put(Integer.toString(light.getDeviceNum()), light);

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

                Room room = rooms.get(roomID);
                room.addBinaryLight(light);
            }
        }
    }

    private static void setupScenes(JSONObject results) throws JSONException {
        scenes.clear();
        sceneNames.clear();

        JSONArray sceneList = results.getJSONArray("scenes");
        for (int index = 0; index < sceneList.length(); index++) {
            JSONObject scene = sceneList.getJSONObject(index);
            if (!scene.has("notification_only")) {
                int sceneNum = scene.getInt("id");
                String sceneName = scene.getString("name");
                scenes.add(new Scene(sceneNum, sceneName));
                sceneNames.add(sceneName);
            }
        }
    }

    public static HashMap<Integer, Room> returnRooms() {
        return rooms;
    }

    public static List<String> getLightIDs() {
        return lightIDs;
    }

    public static List<Scene> getScenes() {
        return scenes;
    }

    public static List<String> getSceneNames() {
        return sceneNames;
    }

    public static HashMap<String, BinaryLight> getLightMap() {
        return lightMap;
    }

}
