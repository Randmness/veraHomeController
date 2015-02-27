package automation.com.veracontroller.pojo;

import java.io.Serializable;

public class BinaryLight implements Serializable {
    private int deviceNum;
    private int roomNum;
    private String name;
    private boolean state = false;

    public BinaryLight(int deviceNum, String name, int roomNum) {
        this.deviceNum = deviceNum;
        this.name = name;
        this.roomNum = roomNum;
    }

    public int getDeviceNum() {
        return this.deviceNum;
    }

    public int getRoomNum() {
        return this.roomNum;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.state;
    }

    public String onOrOff(boolean var) {
        if (var) {
            return "ON";
        }

        return "OFF";
    }

    public String onOrOff() {
        return onOrOff(state);
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
