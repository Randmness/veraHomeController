package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.LoginActivity;
import automation.com.veracontroller.R;
import automation.com.veracontroller.SplashScreen;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.util.RestClient;

public class FetchLocationDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private ProgressDialog dialog;
    private boolean initialEntry;
    private String serialNumber;

    private ArrayList<String> userList = new ArrayList<>();

    public FetchLocationDetailsTask(Activity activity, boolean initialEntry) {
        this.activity = activity;
        this.initialEntry = initialEntry;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(activity, "Fetching Data",
                "Retrieving automation setup...");
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            processResult(RestClient.fetchLocationDetails());
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            return false;
        } finally {
            dialog.dismiss();
        }
        return true;
    }

    protected void processResult(JSONObject results) throws Exception {
        JSONObject unit = results.getJSONArray("units").getJSONObject(0);
        serialNumber = unit.getString("serialNumber");
        String localIP = unit.getString("ipAddress");
        String remoteUrl = unit.getString("active_server");

        updateConstants(serialNumber, localIP, remoteUrl);

        if (initialEntry) {
            JSONArray users = unit.getJSONArray("users");
            for (int index = 0; index < users.length(); index++) {
                userList.add(users.getString(index));
                Log.i("User", userList.get(index));
            }
        }
    }

    protected void updateConstants(String serialNumber, String localUrl, String remoteUrl) {
        String savedLocalUrl = "http://" + localUrl + ":3480/";
        String savedRemoteUrl = "http://" + remoteUrl + "/";

        RestClient.setLocalURL(savedLocalUrl);
        RestClient.setRemoteURL(savedRemoteUrl);


        SharedPreferences sharedPref = activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PreferenceConstants.SERIAL_NUMBER, serialNumber);
        editor.putString(PreferenceConstants.LOCAL_URL, savedLocalUrl);
        editor.putString(PreferenceConstants.REMOTE_URL, savedRemoteUrl);
        editor.commit();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            if (activity instanceof SplashScreen) {
                if (initialEntry) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putStringArrayListExtra(IntentConstants.USER_LIST, userList);
                    intent.putExtra(IntentConstants.INITIAL_LOGIN, true);
                    intent.putExtra(IntentConstants.SERIAL_NUMBER, serialNumber);
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    new FetchConfigurationDetailsTask(activity, true).execute();
                }

            } else if (activity instanceof DeviceActivity) {
                if (initialEntry) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putStringArrayListExtra(IntentConstants.USER_LIST, userList);
                    intent.putExtra(IntentConstants.SERIAL_NUMBER, serialNumber);
                    activity.startActivity(intent);
                }
            }
        } else {
            if (activity instanceof SplashScreen) {
                final SharedPreferences sharedPref = activity.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);

                if (RestClient.getLeverageRemote()) {
                    AlertDialog.Builder webDialog = new AlertDialog.Builder(activity);
                    webDialog.setMessage(R.string.recoveryRemote);
                    webDialog.setCancelable(false);
                    webDialog.setPositiveButton("Attempt\nLocal",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean(PreferenceConstants.LEVERAGE_REMOTE, false);
                                    editor.commit();
                                    RestClient.setLeverageRemote(false);
                                    new FetchConfigurationDetailsTask(activity, true).execute();
                                }
                            });
                    webDialog.setNeutralButton("Retry\nRemote",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    new FetchConfigurationDetailsTask(activity, true).execute();
                                }
                            });
                    webDialog.setNegativeButton("Update\nLocation",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    new FetchLocationDetailsTask(activity, false).execute();
                                }
                            });
                    webDialog.create().show();
                } else {
                    final String password = sharedPref.getString(PreferenceConstants.PASSWORD, null);
                    AlertDialog.Builder webDialog = new AlertDialog.Builder(activity);
                    webDialog.setMessage(R.string.recoveryLocal);
                    webDialog.setCancelable(false);
                    if (password != null) {
                        webDialog.setPositiveButton("Attempt\nRemote",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putBoolean(PreferenceConstants.LEVERAGE_REMOTE, true);
                                        editor.commit();
                                        String serialNumber = sharedPref.getString(PreferenceConstants.SERIAL_NUMBER, null);
                                        String username = sharedPref.getString(PreferenceConstants.USER_NAME, null);
                                        String remoteUrl = sharedPref.getString(PreferenceConstants.REMOTE_URL, null);
                                        RestClient.setRemoteURL(remoteUrl);
                                        RestClient.updateCredentials(username, password, serialNumber);
                                        RestClient.setLeverageRemote(true);
                                        new FetchConfigurationDetailsTask(activity, true).execute();
                                    }
                                });
                    }
                    webDialog.setNeutralButton("Retry\nLocal",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    new FetchConfigurationDetailsTask(activity, true).execute();
                                }
                            });
                    webDialog.setNegativeButton("Update\nLocation",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    new FetchLocationDetailsTask(activity, false).execute();
                                }
                            });
                    webDialog.create().show();
                }
            }
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
    }
}