package automation.com.veracontroller.pojo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by root on 2/22/15.
 */
public class Room implements Serializable {
    private int roomNum;
    private String roomName;

    ArrayList<BinaryLight> lights = new ArrayList<BinaryLight>();

    public Room(int roomNum, String roomName) {
        this.roomNum = roomNum;
        this.roomName = roomName;
    }

    public void addBinaryLight(BinaryLight light) {
        lights.add(light);
    }

    public String getRoomName() {
        return this.roomName;
    }

    public ArrayList<BinaryLight> getLights() {
        return this.lights;
    }
}
