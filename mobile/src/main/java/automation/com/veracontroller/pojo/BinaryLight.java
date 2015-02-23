package automation.com.veracontroller.pojo;

import java.io.Serializable;

public class BinaryLight implements Serializable {
    private int deviceNum;
    private boolean state = false;

    public static final String DEVICE_TYPE = "urn:schemas-upnp-org:device:BinaryLight:1";
    public static final String SERVICE_ID = "urn:upnp-org:serviceId:SwitchPower1";

    public BinaryLight(int deviceNum) {
        this.deviceNum = deviceNum;
    }

    public int getDeviceNum() {
        return this.deviceNum;
    }

    public boolean isEnabled() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
