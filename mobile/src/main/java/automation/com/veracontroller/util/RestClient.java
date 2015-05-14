package automation.com.veracontroller.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import automation.com.veracontroller.util.support.ServiceTypeEnum;


abstract public class RestClient {
    private static final String LOCATION_URL = "http://sta1.mios.com/";
    private static final String LOCATION_QUERY = "locator_json.php";
    private static final String DATA_REQUEST_QUERY = "data_request?";

    //make configurable to swap based on property
    private static String LOCAL_URL;
    private static String REMOTE_URL;
    private static Boolean LEVERAGE_REMOTE = false;

    private static String CREDENTIAL_PATH = "[$USER_NAME]/[$PWD]/[$SERIAL]/";
    private static int CONNECTION_TIMEOUT = 10000;

    public static void setLocalURL(String localUrl) {
        LOCAL_URL = localUrl;
    }

    public static void setRemoteURL(String remoteUrl) {
        REMOTE_URL = remoteUrl;
    }

    public static void setLeverageRemote(boolean leverageRemote) {
        LEVERAGE_REMOTE = leverageRemote;
    }

    public static boolean getLeverageRemote() { return LEVERAGE_REMOTE; }

    public static void updateCredentials(String userName, String pwd, String serial) {
        CREDENTIAL_PATH = CREDENTIAL_PATH.replace("[$USER_NAME]", userName);
        CREDENTIAL_PATH = CREDENTIAL_PATH.replace("[$PWD]", pwd);
        CREDENTIAL_PATH = CREDENTIAL_PATH.replace("[$SERIAL]", serial);
    }

    private static String urlPreference() {
        if (LEVERAGE_REMOTE) {
            return REMOTE_URL + CREDENTIAL_PATH;
        }

        return LOCAL_URL;
    }

    public static boolean executeSwitchCommand(boolean on, int deviceID) {
        boolean success = true;
        int target = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "lu_action");
        map.put("DeviceNum", deviceID);
        map.put("serviceId", ServiceTypeEnum.SWITCH_LIGHT.toString());
        map.put("action", "SetTarget");
        if (on) {
            target = 1;
        }
        map.put("newTargetValue", target);

        try {
            executeCommand(urlPreference(), DATA_REQUEST_QUERY, map);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public static boolean executeSceneCommand(int sceneID) {
        boolean success = true;

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "lu_action");
        map.put("SceneNum", sceneID);
        map.put("serviceId", ServiceTypeEnum.SCENE.toString());
        map.put("action", "RunScene");

        try {
            executeCommand(urlPreference(), DATA_REQUEST_QUERY, map);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public static JSONObject fetchConfigurationDetails() throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "user_data2");
        return executeCommand(urlPreference(), DATA_REQUEST_QUERY, map);
    }

    public static JSONObject attemptAuthentication(String username, String password, String serial) throws JSONException {
        StringBuilder remoteUrl = new StringBuilder(REMOTE_URL);
        remoteUrl.append(username);
        remoteUrl.append("/");
        remoteUrl.append(password);
        remoteUrl.append("/");
        remoteUrl.append(serial);
        remoteUrl.append("/");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "user_data2");
        return executeCommand(remoteUrl.toString(), DATA_REQUEST_QUERY, map);
    }

    public static JSONObject fetchLocationDetails() throws JSONException {
        return executeCommand(LOCATION_URL, LOCATION_QUERY, null);
    }

    public static JSONObject executeCommand(String url, String query, Map<String, Object> params) throws JSONException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        InputStream content = null;
        try {
            if (params != null) {
                for (String key : params.keySet()) {
                    query += "&" + URLEncoder.encode(key, "UTF-8") + "="
                            + URLEncoder.encode(String.valueOf(params.get(key)), "UTF-8");
                }
            }

            Log.i("Query: ", url + query);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url + query);
            HttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                content = response.getEntity().getContent();
                reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e("Failure", "Failed to execute command.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                content.close();
            } catch (IOException e){}

            try {
                reader.close();
            } catch (IOException e) {}
        }

        Log.i("Output: ", builder.toString());
        return new JSONObject(builder.toString());
    }
}
