package automation.com.veracontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import automation.com.veracontroller.async.RequestDataThread;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;


public class SplashActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int SPLASH_DELAY = 2000;

    private GoogleApiClient googleClient;
    private ProgressDialog activityDialog;
    private boolean firstEntry = true;
    MessageReceiver messageReceiver;

    private boolean isAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAvailable = true;
        if (messageReceiver != null) {
            registerReceiver(messageReceiver, new IntentFilter(Intent.ACTION_SEND));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAvailable = false;
        try {
            unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (firstEntry) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (activityDialog == null) {
                        activityDialog = new ProgressDialog(SplashActivity.this);
                        activityDialog.setMessage("Fetching setup.");
                        activityDialog.setCancelable(false);
                        activityDialog.setTitle("Starting Up");
                    }
                    activityDialog.show();
                    new RequestDataThread(DataPathEnum.WEARABLE_SPLASH_DATA_REQUEST, "Requesting data.", googleClient).start();
                    firstEntry = false;
                }
            }, SPLASH_DELAY);
        }
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }

        try {
            unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e) {
        }
        isAvailable = false;
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(IntentConstants.DATA_PATH);
            switch (DataPathEnum.fromPath(message)) {
                case WEARABLE_SPLASH_DATA_RESPONSE:
                    ArrayList<BinaryLight> lights = intent.getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
                    ArrayList<Scene> scenes = intent.getParcelableArrayListExtra(IntentConstants.SCENE_LIST);

                    Intent changeIntent = new Intent(SplashActivity.this, DeviceActivity.class);
                    changeIntent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lights);
                    changeIntent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, scenes);
                    SplashActivity.this.startActivity(changeIntent);
                    finish();
                    activityDialog.dismiss();
                    break;
                case WEARABLE_SPLASH_DATA_ERROR:
                    if (activityDialog.isShowing()) {
                        activityDialog.dismiss();
                    }

                    if(isAvailable) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
                        alertDialog.setMessage("Failed to retrieve starting configuration.");
                        alertDialog.setPositiveButton("Retry",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        activityDialog.show();
                                        new RequestDataThread(DataPathEnum.WEARABLE_SPLASH_DATA_REQUEST, "Requesting data.", googleClient).start();
                                    }
                                });
                        alertDialog.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });

                        alertDialog.create().show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
