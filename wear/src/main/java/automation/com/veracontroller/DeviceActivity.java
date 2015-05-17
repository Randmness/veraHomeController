package automation.com.veracontroller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lights = getIntent().getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
        scenes = getIntent().getParcelableArrayListExtra(IntentConstants.SCENE_LIST);

        //Log.i("Lights", lights.size()+"");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(this, lights, scenes, googleApiClient));
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND_MULTIPLE);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("Connected", "Connected to wearable device.");
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
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
            Log.i("MessageLister", "Message received on path: "+message);
            if (DataPathEnum.fromPath(message) == DataPathEnum.WEARABLE_CONFIG_DATA_RESPONSE) {
                ArrayList<BinaryLight> lights = intent.getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
                ArrayList<Scene> scenes = intent.getParcelableArrayListExtra(IntentConstants.SCENE_LIST);
            }
        }
    }

}
