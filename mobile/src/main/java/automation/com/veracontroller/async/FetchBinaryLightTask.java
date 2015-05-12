package automation.com.veracontroller.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import automation.com.veracontroller.adapter.BinaryLightListAdapter;
import automation.com.veracontroller.util.RestClient;
import automation.com.veracontroller.util.RoomDataUtil;

public class FetchBinaryLightTask extends AsyncTask<Void, Void, Boolean> {
    SwipeRefreshLayout swipe;
    private Activity activity;
    private BinaryLightListAdapter adapter;

    public FetchBinaryLightTask(Activity activity, BinaryLightListAdapter adapter, SwipeRefreshLayout swipe) {
        this.activity = activity;
        this.adapter = adapter;
        this.swipe = swipe;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            adapter.setList(RoomDataUtil.getLights(RestClient.fetchConfigurationDetails()));
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