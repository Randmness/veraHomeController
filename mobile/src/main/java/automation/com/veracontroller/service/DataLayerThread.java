package automation.com.veracontroller.service;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class DataLayerThread extends Thread {
    private String path;
    private DataMap dataMap;
    private GoogleApiClient googleClient;

    public DataLayerThread(String p, DataMap data, GoogleApiClient googleClient) {
        this.path = p;
        this.dataMap = data;
        this.googleClient = googleClient;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
        for (Node node : nodes.getNodes()) {
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient,request).await();
            if (result.getStatus().isSuccess()) {
                Log.i("Successful Data Sent: ",  dataMap + " sent to: " + node.getDisplayName());
            } else {
                Log.v("Failed Data Sent:", "ERROR: failed to send DataMap");
            }
        }
    }
}