package automation.com.veracontroller.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import java.util.HashMap;

import automation.com.veracontroller.DeviceActivity;
import automation.com.veracontroller.adapter.CustomListAdapter;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.singleton.RoomData;
import automation.com.veracontroller.util.RestClient;

public class FetchBinaryLightTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private CustomListAdapter adapter;
    SwipeRefreshLayout swipe;

    public FetchBinaryLightTask(Activity activity, CustomListAdapter adapter, SwipeRefreshLayout swipe) {
        this.activity = activity;
        this.adapter = adapter;
        this.swipe = swipe;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            adapter.setList(RoomData.getLights(RestClient.fetchConfigurationDetails()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            adapter.clear();
            adapter.addAll(adapter.getLights());
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
        swipe.setRefreshing(false);
    }
}