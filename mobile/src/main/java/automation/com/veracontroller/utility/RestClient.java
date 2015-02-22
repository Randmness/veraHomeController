package automation.com.veracontroller.utility;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;


abstract public class RestClient {
    private static final String LOCATION_URL = "https://sta1.mios.com/";
    private static final String LOCATION_QUERY = "locator_json.php";
    private static final String DATA_REQUEST = "data_request?";
    private static String LOCAL_URL;
    private static String REMOTE_URL;
    private static String CREDENTIAL_PATH = "[$USER_NAME]/[$PWD]/[$SERIAL]/";

    private static Boolean LEVERAGE_REMOTE = true;

    public static void setLocalURL(String localUrl) {
        LOCAL_URL = localUrl;
    }

    public static void setRemoteURL(String remoteUrl) {
        REMOTE_URL = remoteUrl;
    }

    public static void updateCredentials(String userName, String pwd, String serial) {
        CREDENTIAL_PATH.replace("[$USER_NAME]", userName);
        CREDENTIAL_PATH.replace("[$PWD]", pwd);
        CREDENTIAL_PATH.replace("[$SERIAL]", serial);
    }

    public static JSONObject fetchLocationDetails() throws JSONException {
        return executeCommand(LOCATION_URL, LOCATION_QUERY, null);
    }

    public static JSONObject executeCommand(String url, String query, Map<String, Object> params) throws JSONException {
        StringBuilder builder = new StringBuilder();
        try {
            if (params != null) {
                for (String key : params.keySet()) {
                    query += "&" + URLEncoder.encode(key, "UTF-8") + "="
                            + URLEncoder.encode(String.valueOf(params.get(key)), "UTF-8");
                }
            }

            Log.i("Query: ", url + query);

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url + query);
            HttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e("Failure", "Failed to execute command.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("Output: ", builder.toString());
        return new JSONObject(builder.toString());
    }

}
