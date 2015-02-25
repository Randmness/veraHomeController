package automation.com.veracontroller.pojo.support;

import android.bluetooth.BluetoothClass;

/**
 * Created by root on 2/22/15.
 */
public enum DeviceTypeEnum {
    BINARY_LIGHT("urn:schemas-upnp-org:device:BinaryLight:1");

    String deviceType;

    DeviceTypeEnum(String deviceType) {
        this.deviceType = deviceType;
    }

    public String toString() {
        return deviceType;
    }
}
