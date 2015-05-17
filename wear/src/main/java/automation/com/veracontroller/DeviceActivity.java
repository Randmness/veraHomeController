package automation.com.veracontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.adapter.ViewPagerAdapter;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

public class DeviceActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private List<BinaryLight> lights = new ArrayList<>();
    private List<Scene> scenes = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private ViewPagerAdapter pagerAdapter;
    private ProgressDialog dialog;

    private DismissOverlayView mDismissOverlay;

    private static final int ADAPTER_UPDATE = 0;

    private DeviceActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            lights = savedInstanceState.getParcelableArrayList(IntentConstants.LIGHT_LIST);
            scenes = savedInstanceState.getParcelableArrayList(IntentConstants.SCENE_LIST);
        } else {
            lights = getIntent().getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
            scenes = getIntent().getParcelableArrayListExtra(IntentConstants.SCENE_LIST);
        }

        //Log.i("Lights", lights.size()+"");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Executing..");

        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pagerAdapter = new ViewPagerAdapter(this, lights, scenes, googleApiClient, dialog);
        pager.setAdapter(pagerAdapter);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        activity = this;
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND_MULTIPLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, messageFilter);
    }

    private BroadcastReceiver resultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(IntentConstants.DATA_PATH);
                Log.i("MessageLister", "Message received on path: " + message);
                if (DataPathEnum.fromPath(message) == DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE) {
                    final List<BinaryLight> newLights = intent.getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
                    final List<Scene> newScenes = intent.getParcelableArrayListExtra(IntentConstants.SCENE_LIST);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.updateUI(newLights, newScenes);
                        }
                    });
                }
            }
        };

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        if (resultReceiver != null) {
            registerReceiver(resultReceiver, new IntentFilter(Intent.ACTION_SEND));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("onPause", "onPause called");
        try {
            unregisterReceiver(resultReceiver);
        } catch (IllegalArgumentException e) {
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("Connected", "Connected to wearable device.");
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        Log.i("onStop","onStop called.");
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        try {
            unregisterReceiver(resultReceiver);
        } catch (IllegalArgumentException e) {
        }
        finish();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(IntentConstants.LIGHT_LIST,
                (ArrayList) pagerAdapter.getBinaryListAdapter().getLights());
        outState.putParcelableArrayList(IntentConstants.SCENE_LIST,
                (ArrayList) pagerAdapter.getSceneListAdapter().getScenes());
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    private void updateUI(List<BinaryLight> newLights, List<Scene> newScenes) {
        dialog.dismiss();
        Log.i("Updating list", "Updating list" + newLights.get(3).onOrOff());
        pagerAdapter.updateLights(newLights);
        pagerAdapter.getBinaryListAdapter().updateLights(newLights);

    }
}
