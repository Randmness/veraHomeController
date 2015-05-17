package automation.com.veracontroller.enums;

/**
 * Created by mrand on 5/16/15.
 */
public enum DataPathEnum {
    WEARABLE_DEVICE_ACTIVITY_LAUNCH("/wearable_device_activity_launch"),
    WEARABLE_SPLASH_DATA_REQUEST("/wearable_splash_data_request"),
    WEARABLE_SPLASH_DATA_RESPONSE("/wearable_splash_data_response"),
    WEARABLE_SPLASH_ERROR_NOT_SETUP_RESPONSE("/wearable_splash_error_not_setup_response"),
    WEARABLE_SPLASH_DATA_ERROR("/wearable_splash_data_error"),
    WEARABLE_CONFIG_DATA_REQUEST("/wearable_config_data_request"),
    WEARABLE_CONFIG_DATA_RESPONSE("/wearable_config_data_response"),
    WEARABLE_CONFIG_DATA_ERROR("/wearable_config_data_error"),
    WEARABLE_DEVICE_LIGHT_TOGGLE("/wearable_device_light_toggle"),
    WEARABLE_DEVICE_SCENE_EXECUTION("/wearable_device_scene_execution");

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
