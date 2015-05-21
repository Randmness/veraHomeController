package automation.com.veracontroller.enums;

/**
 * Created by root on 2/22/15.
 */
public enum DeviceTypeEnum {
    BINARY_LIGHT("urn:schemas-upnp-org:device:BinaryLight:1"),
    DIMMABLE_LIGHT("urn:schemas-upnp-org:device:DimmableLight:1"),
    UNKNOWN("DEFAULT");

    String deviceType;

    DeviceTypeEnum(String deviceType) {
        this.deviceType = deviceType;
    }

    public static DeviceTypeEnum findDevice(String deviceType) {
        for (DeviceTypeEnum device : values()) {
            if (device.toString().equalsIgnoreCase(deviceType)) {
                return device;
            }
        }

        return UNKNOWN;
    }

    public String toString() {
        return deviceType;
    }
}
