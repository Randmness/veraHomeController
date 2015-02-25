package automation.com.veracontroller.singleton;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.pojo.support.DeviceTypeEnum;
import automation.com.veracontroller.pojo.support.ServiceTypeEnum;

/**
 * Created by root on 2/22/15.
 */
abstract public class RoomData {

    private static HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();

    public static void resetMap(JSONObject results) throws JSONException {
        rooms.clear();

        JSONArray roomList = results.getJSONArray("rooms");
        for (int index = 0; index < roomList.length(); index++) {
            JSONObject room = roomList.getJSONObject(index);
            int roomID = room.getInt("id");
            String roomName = room.getString("name");
            rooms.put(roomID, new Room(roomID, roomName));

            Log.i("ROOMS: ", roomID + "-" + roomName);
        }

        JSONArray devices = results.getJSONArray("devices");
        for (int index = 0; index < devices.length(); index++) {
            JSONObject device = devices.getJSONObject(index);
            String deviceType = device.getString("device_type");
            if (deviceType != null && deviceType.equals(DeviceTypeEnum.BINARY_LIGHT.toString())) {
                int deviceID = device.getInt("id");
                int roomID = device.getInt("room");
                String name = device.getString("name");

                BinaryLight light = new BinaryLight(deviceID, name);

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

                Log.i("DEVICE: ", deviceID + "-" + room.getRoomName()+", "+light.onOrOff());
            }
        }
    }

    public static HashMap<Integer, Room> returnRooms() {
        return rooms;
    }

}
