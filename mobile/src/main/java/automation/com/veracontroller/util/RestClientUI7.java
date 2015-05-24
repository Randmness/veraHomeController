package automation.com.veracontroller.util;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import automation.com.veracontroller.constants.ConnectionConstants;
import automation.com.veracontroller.enums.ServiceTypeEnum;
import automation.com.veracontroller.enums.VeraType;
import automation.com.veracontroller.pojo.session.Session;
import automation.com.veracontroller.pojo.session.SessionUI7;


abstract public class RestClientUI7 {
    private static Session SESSION;

    public static SessionUI7 initialSetup(String username, String password) throws Exception {
        JSONObject authTokens = retrieveAuthTokens(username, password);
        String authToken = authTokens.getString("Identity");
        String authSigToken = authTokens.getString("IdentitySignature");
        String serverAccount = authTokens.getString("Server_Account");

        String sessionToken = retrieveSessionToken("vera-us-oem-authd11.mios.com", authToken, authSigToken);

        byte[] data = Base64.decode(authToken, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject(new String(data, "UTF-8"));
        String pkAccount = jsonObject.getString("PK_Account");
        JSONObject locator = retrieveLocatorViaRemote(serverAccount, pkAccount, sessionToken);

        String pkDevice = locator.getJSONArray("Devices").getJSONObject(0).getString("PK_Device");
        JSONObject relay = getRelayServer("vera-us-oem-device11.mios.com", pkDevice, sessionToken);
        String localIP = relay.getString("InternalIP");
        String serverRelay = relay.getString("Server_Relay");

        String remoteUrl = ConnectionConstants.UI7_REMOTE_URL_PATTERN.replace("[$SERVER_RELAY]", serverRelay);
        remoteUrl = remoteUrl.replace("[$PK_DEVICE]", pkDevice);

        SessionUI7 sessionUI7 = new SessionUI7();
        sessionUI7.setUserName(username);
        sessionUI7.setPassword(password);
        sessionUI7.setSessionToken(retrieveSessionToken(serverRelay, authToken, authSigToken));
        sessionUI7.setServerRelay(serverRelay);
        sessionUI7.setSerialNumber(pkDevice);
        sessionUI7.setRemoteUrl(remoteUrl);
        sessionUI7.setLocalUrl("http://" + localIP + ":3480/");
        sessionUI7.setSystemType(VeraType.VERA_UI7);
        return sessionUI7;
    }

    public static void setSession(Session session) {
        SESSION = session;
    }

    public static Session getSession() {
        return SESSION;
    }

    public static JSONObject fetchLocationDetails() throws Exception {
        JSONObject units = executeJSONCommand(ConnectionConstants.LOCATION_URL,
                ConnectionConstants.LOCATION_QUERY, null, null);
        return units.getJSONArray("units").getJSONObject(0);
    }

    public static void retrieveNewSession() throws Exception {
        SessionUI7 session = (SessionUI7) SESSION;
        String username = session.getUserName();
        String password = session.getPassword();
        String serverRelay = session.getServerRelay();

        JSONObject authTokens = retrieveAuthTokens(username, password);
        String authToken = authTokens.getString("Identity");
        String authSigToken = authTokens.getString("IdentitySignature");

        session.setSessionToken(retrieveSessionToken(serverRelay,
                authToken, authSigToken));
    }

    //1
    public static JSONObject retrieveAuthTokens(String username, String password) throws Exception {
        String hashed = new String(Hex.encodeHex(DigestUtils.sha1(username.toLowerCase() + password + ConnectionConstants.PASSWORD_SEED)));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("SHA1Password", hashed);
        map.put("PK_Oem", 1);
        return executeJSONCommand(ConnectionConstants.AUTH_TOKEN_URL, username.toLowerCase() + "?", map, null);
    }

    //2
    public static String retrieveSessionToken(String url, String mmsAuth, String mmsAuthSig) throws Exception {
        Header[] headers = {new BasicHeader("MMSAuth", mmsAuth) ,new BasicHeader("MMSAuthSig", mmsAuthSig)};
        return executeCommand(ConnectionConstants.SESSION_SERVER.replace("[$SESSION_URL]", url), "token", null, headers);
    }

    //3
    public static JSONObject retrieveLocatorViaRemote(String serverAccount, String pkAccount, String sessionToken) throws Exception {
        String locatorUrl = ConnectionConstants.SERVER_ACCOUNT.replace("[$SERVER_ACCOUNT]", serverAccount);
        locatorUrl = locatorUrl.replace("[$PK_ACCOUNT]", pkAccount);

        Header[] headers = {new BasicHeader("MMSSession", sessionToken)};
        return executeJSONCommand(locatorUrl, "devices", null, headers);
    }

    //4
    public static JSONObject getRelayServer(String serverDevice, String pkDevice, String sessionToken) throws Exception {
        Header[] headers = {new BasicHeader("MMSSession", sessionToken)};

        return executeJSONCommand(ConnectionConstants.SERVER_RELAY.replace("[$SERVER_DEVICE]",
                serverDevice), pkDevice, null, headers);
    }

    private static String getSessionToken() throws Exception{
        SessionUI7 sessionUI7 = (SessionUI7) SESSION;
        Log.i("Expired", sessionUI7.isExpired()+"");
        if (sessionUI7.isExpired()) {
            retrieveNewSession();
        }

        return sessionUI7.getSessionToken();
    }

    private static Header[] retrieveSessionHeader() throws Exception {
        if (SESSION.getSystemType() == VeraType.VERA_UI7 && SESSION.getLeverageRemote()) {
            Header[] headers = {new BasicHeader("MMSSession", getSessionToken())};
            return headers;
        } else {
            return null;
        }
    }

    private static String urlPreference() {
        if (SESSION.getLeverageRemote()) {
            return SESSION.getRemoteUrl();
        }
        return SESSION.getLocalUrl();
    }

    public static JSONObject attemptAuthentication(String username, String password) throws Exception {
        StringBuilder remoteUrl = new StringBuilder(SESSION.getRemoteUrl());
        remoteUrl.append(username);
        remoteUrl.append("/");
        remoteUrl.append(password);
        remoteUrl.append("/");
        remoteUrl.append(SESSION.getSerialNumber());
        remoteUrl.append("/");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "user_data2");
        return executeJSONCommand(remoteUrl.toString(), ConnectionConstants.DATA_REQUEST_QUERY, map, null);
    }

    public static JSONObject fetchConfigurationDetails() throws Exception {
        Header[] headers = retrieveSessionHeader();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "user_data2");
        return executeJSONCommand(urlPreference(), ConnectionConstants.DATA_REQUEST_QUERY, map, headers);
    }

    public static boolean executeSwitchCommand(boolean on, int deviceID) throws Exception{
        boolean success = true;
        Header[] headers = retrieveSessionHeader();
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
            executeCommand(urlPreference(), ConnectionConstants.DATA_REQUEST_QUERY, map, headers);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public static boolean executeSceneCommand(int sceneID) throws Exception {
        boolean success = true;
        Header[] headers = retrieveSessionHeader();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", "lu_action");
        map.put("SceneNum", sceneID);
        map.put("serviceId", ServiceTypeEnum.SCENE.toString());
        map.put("action", "RunScene");

        try {
            executeCommand(urlPreference(), ConnectionConstants.DATA_REQUEST_QUERY, map, headers);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public static JSONObject executeJSONCommand(String url, String query, Map<String, Object> params,
                                                Header[] headers) throws Exception {
        return new JSONObject(executeCommand(url, query, params, headers));
    }

    public static String executeCommand(String url, String query, Map<String, Object> params,
                                        Header[] headers) throws Exception {
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
            HttpConnectionParams.setConnectionTimeout(httpParams, ConnectionConstants.CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, ConnectionConstants.CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url + query);
            httpGet.setHeaders(headers);
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
                throw new Exception("Call failed to execute.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                content.close();
            } catch (IOException e) {
            }

            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        Log.i("Output: ", builder.toString());
        return builder.toString();
    }
}
