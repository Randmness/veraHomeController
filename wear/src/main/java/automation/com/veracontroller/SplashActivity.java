package automation.com.veracontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

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
    private ProgressDialog dialog;
    private boolean firstEntry = true;
    MessageReceiver messageReceiver;

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

        if (messageReceiver != null) {
            registerReceiver(messageReceiver, new IntentFilter(Intent.ACTION_SEND));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e){
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (firstEntry) {
            if (dialog == null) {
                dialog = new ProgressDialog(this);
                dialog.setMessage("Fetching configuration details...");
                dialog.setCancelable(false);
                dialog.setTitle("       Starting Up");
            }
            dialog.show();
            new RequestDataThread(DataPathEnum.WEARABLE_SPLASH_DATA_REQUEST, "Requesting data.", googleClient).start();
                firstEntry = false;
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
        } catch (IllegalArgumentException e){
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(IntentConstants.DATA_PATH);
            if (DataPathEnum.fromPath(message) == DataPathEnum.WEARABLE_SPLASH_DATA_RESPONSE) {
                ArrayList<BinaryLight> lights = intent.getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
                ArrayList<Scene> scenes = intent.getParcelableArrayListExtra(IntentConstants.SCENE_LIST);

                Intent changeIntent = new Intent(SplashActivity.this, DeviceActivity.class);
                changeIntent.putParcelableArrayListExtra(IntentConstants.LIGHT_LIST, lights);
                changeIntent.putParcelableArrayListExtra(IntentConstants.SCENE_LIST, scenes);
                SplashActivity.this.startActivity(changeIntent);
                finish();
                dialog.dismiss();
            }
        }
    }
}
