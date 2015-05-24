package automation.com.veracontroller.constants;

/**
 * Created by mrand on 5/16/15.
 */
public class ConnectionConstants {
    public static final String AUTH_TOKEN_URL = "https://vera-us-oem-autha11.mios.com/autha/auth/username/";
    public static final String SESSION_SERVER = "https://[$SESSION_URL]/info/session/";
    public static final String SERVER_ACCOUNT = "https://[$SERVER_ACCOUNT]/account/account/account/[$PK_ACCOUNT]/";
    public static final String SERVER_RELAY = "https://[$SERVER_DEVICE]/device/device/device/";
    public static final String UI7_REMOTE_URL_PATTERN = "https://[$SERVER_RELAY]/relay/relay/relay/device/[$PK_DEVICE]/port_3480/";
    public static final String DATA_REQUEST_QUERY = "data_request?";
    public static final String PASSWORD_SEED = "oZ7QE6LcLJp6fiWzdqZc";

    public static final String LOCATION_URL = "http://sta1.mios.com/";
    public static final String LOCATION_QUERY = "locator_json.php";

    public static Boolean LEVERAGE_REMOTE = false;

    public static String CREDENTIAL_PATH = "[$USER_NAME]/[$PWD]/[$SERIAL]/";
    public static int CONNECTION_TIMEOUT = 10000;

}
