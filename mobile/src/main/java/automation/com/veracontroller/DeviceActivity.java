package automation.com.veracontroller;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import automation.com.veracontroller.async.FetchConfigurationDetailsTask;
import automation.com.veracontroller.async.FetchLocationDetailsTask;
import automation.com.veracontroller.async.ToggleBinaryLightTask;
import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.constants.PreferenceConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.fragments.support.DevicePagerAdapter;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import automation.com.veracontroller.service.DataLayerThread;
import automation.com.veracontroller.service.PollingService;
import automation.com.veracontroller.util.RestClient;

public class DeviceActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int POLLING_INTERVAL = 5000;
    public static final int MSG_SERVICE_OBJ = 2;

    //startup constraints
    private List<BinaryLight> lights = new ArrayList<>();
    private List<Scene> scenes = new ArrayList<>();

    private PollingService service;
    private SharedPreferences sharedPref;
    private boolean pollingEnabled;
    private GoogleApiClient googleClient;
    private Gson gson = new Gson();

    Handler mHandler = new Handler(/* default looper */) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SERVICE_OBJ:
                    if (service == null) {
                        service = (PollingService) msg.obj;
                        service.setActivity(DeviceActivity.this);
                        DeviceActivity.this.scheduleJob();
                    }
            }
        }
    };

    /**
     * Binary light click.
     *
     * @param view
     */
    public void onToggleClicked(View view) {
        ToggleButton aSwitch = ((ToggleButton) view);
        BinaryLight clickedLight = (BinaryLight) view.getTag(R.string.objectHolder);
        new ToggleBinaryLightTask(view.getContext(), clickedLight, aSwitch.isChecked()).execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        sharedPref = DeviceActivity.this.getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
        pollingEnabled = sharedPref.getBoolean(PreferenceConstants.POLLING_ENABLED, false);

        lights = getIntent().getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
        scenes = getIntent().getParcelableArrayListExtra(IntentConstants.SCENE_LIST);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new DevicePagerAdapter(getSupportFragmentManager(), (ArrayList) lights, (ArrayList) scenes));
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (pollingEnabled) {
            createPollingService();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyAllJobs();
    }

    private void createPollingService() {
        if (service == null) {
            Intent startServiceIntent = new Intent(this, PollingService.class);
            startServiceIntent.putExtra(IntentConstants.MESSENGER, new Messenger(mHandler));
            startService(startServiceIntent);
        } else {
            scheduleJob();
        }
    }

    private void destroyAllJobs() {
        Log.i("Jobs", "Destroying all jobs.");
        JobScheduler tm =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (service != null && pollingEnabled) {
            this.scheduleJob();
        }
    }

    @Override
    protected void onDestroy() {
        destroyAllJobs();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (RestClient.getLeverageRemote()) {
            menu.findItem(R.id.enableRemote).setChecked(true);
            menu.findItem(R.id.updateRemoteLogin).setVisible(true);
        } else {
            menu.findItem(R.id.enableRemote).setChecked(false);
            menu.findItem(R.id.updateRemoteLogin).setVisible(false);
        }
        menu.findItem(R.id.enablePolling).setChecked(pollingEnabled);
        return true;
    }

    public void pollingUpdate(PollingService service) {
        new FetchConfigurationDetailsTask(false, getSupportFragmentManager().getFragments(), service).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.enablePolling:
                SharedPreferences.Editor editor = sharedPref.edit();
                if (item.isChecked()) {
                    destroyAllJobs();
                    pollingEnabled = false;
                } else {
                    pollingEnabled = true;
                    createPollingService();
                }
                editor.putBoolean(PreferenceConstants.POLLING_ENABLED, pollingEnabled);
                editor.commit();
                break;
            case R.id.updateLocationDetails:
                AlertDialog.Builder webDialog = new AlertDialog.Builder(DeviceActivity.this);
                webDialog.setMessage(R.string.updateLocation);
                webDialog.setCancelable(true);
                webDialog.setPositiveButton("Update Location Details",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FetchLocationDetailsTask(DeviceActivity.this, false).execute();
                            }
                        });
                webDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertWeb = webDialog.create();
                alertWeb.show();
                break;
            case R.id.sendFeedback:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"RandmDeveloper@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Smart Abode: Feedback");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Send Email"));
                break;
            case R.id.updateRemoteLogin:
                AlertDialog.Builder loginDialog = new AlertDialog.Builder(DeviceActivity.this);
                loginDialog.setMessage(R.string.updateRemote);
                loginDialog.setCancelable(false);
                loginDialog.setPositiveButton("Update Credentials",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FetchLocationDetailsTask(DeviceActivity.this, true).execute();
                            }
                        });
                loginDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog loginWeb = loginDialog.create();
                loginWeb.show();
                break;
            case R.id.enableRemote:
                SharedPreferences sharedPref = getSharedPreferences(PreferenceConstants.PREF_KEY, Context.MODE_PRIVATE);
                if (item.isChecked()) {
                    editor = sharedPref.edit();
                    editor.putBoolean(PreferenceConstants.LEVERAGE_REMOTE, false);
                    editor.commit();
                    RestClient.setLeverageRemote(false);
                } else {
                    String password = sharedPref.getString(PreferenceConstants.PASSWORD, null);

                    if (password == null) {
                        AlertDialog.Builder remoteDialog = new AlertDialog.Builder(DeviceActivity.this);
                        remoteDialog.setMessage(R.string.firstTimeRemote);
                        remoteDialog.setCancelable(false);
                        remoteDialog.setPositiveButton("Update Credentials",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new FetchLocationDetailsTask(DeviceActivity.this, true).execute();
                                    }
                                });
                        remoteDialog.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog remoteWeb = remoteDialog.create();
                        remoteWeb.show();
                    } else {
                        String username = sharedPref.getString(PreferenceConstants.USER_NAME, null);
                        String serial = sharedPref.getString(PreferenceConstants.SERIAL_NUMBER, null);
                        String remoteUrl = sharedPref.getString(PreferenceConstants.REMOTE_URL, null);
                        RestClient.setLeverageRemote(true);
                        RestClient.setRemoteURL(remoteUrl);
                        RestClient.updateCredentials(username, password, serial);

                        editor = sharedPref.edit();
                        editor.putBoolean(PreferenceConstants.LEVERAGE_REMOTE, true);
                        editor.commit();
                    }
                }
                invalidateOptionsMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scheduleJob() {
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, PollingService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPeriodic(POLLING_INTERVAL);
        service.scheduleJob(builder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("Connected", "Connected to wearable device.");

        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        String lightList = gson.toJson(lights,  new TypeToken<ArrayList<BinaryLight>>(){}.getType());
        String sceneList = gson.toJson(scenes, new TypeToken<ArrayList<Scene>>(){}.getType());
        dataMap.putString(DataMapConstants.LIGHT_LIST, lightList);
        dataMap.putString(DataMapConstants.SCENE_LIST, sceneList);
        dataMap.putString("UUID", UUID.randomUUID().toString());
        new DataLayerThread(DataPathEnum.WEARABLE_DEVICE_ACTIVITY_LAUNCH.toString(), dataMap, googleClient).start();
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }
}
