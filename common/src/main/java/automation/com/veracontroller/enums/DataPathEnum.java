package automation.com.veracontroller.enums;

/**
 * Created by mrand on 5/16/15.
 */
public enum DataPathEnum {
    WEARABLE_DEVICE_ACTIVITY_LAUNCH("/wearable_device_activity_launch");

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
