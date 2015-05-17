package automation.com.veracontroller.async;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import automation.com.veracontroller.enums.DataPathEnum;

public class RequestDataThread extends Thread {
    private String path;
    private String message;
    private GoogleApiClient googleClient;

    public RequestDataThread(DataPathEnum dataPath, String message, GoogleApiClient googleClient) {
        this.path = dataPath.toString();
        this.message = message;
        this.googleClient = googleClient;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.i("RequestDataThread", "Message: {" + message + "} sent to: " + node.getDisplayName());
            }
            else {
                // Log an error
                Log.i("RequestDataThread", "ERROR: failed to send Message");
            }
        }
    }
}
