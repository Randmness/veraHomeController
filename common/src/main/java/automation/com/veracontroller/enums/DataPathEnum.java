package automation.com.veracontroller.enums;

/**
 * Created by mrand on 5/16/15.
 */
public enum DataPathEnum {
    WEARABLE_DEVICE_ACTIVITY_LAUNCH("/wearable_device_activity_launch"),
    WEARABLE_SPLASH_DATA_REQUEST("/wearable_splash_data_request"),
    WEARABLE_SPLASH_DATA_RESPONSE("/wearable_splash_data_response");

    private String dataPath;

    DataPathEnum(String dataPath) {
        this.dataPath = dataPath;
    }

    public static DataPathEnum fromPath(String dataPath) {
        for (DataPathEnum path : values()) {
            if (path.toString().equalsIgnoreCase(dataPath)) {
                return path;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.dataPath;
    }
}
