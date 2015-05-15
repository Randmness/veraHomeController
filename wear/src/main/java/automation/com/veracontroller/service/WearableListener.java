package automation.com.veracontroller.service;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by mrand on 5/15/15.
 */
public class WearableListener extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.i("Message Received: ", path);
    }
}
