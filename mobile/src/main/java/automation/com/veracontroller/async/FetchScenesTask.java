package automation.com.veracontroller.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import automation.com.veracontroller.adapter.SceneListAdapter;
import automation.com.veracontroller.util.RoomDataUtil;
import automation.com.veracontroller.util.RestClient;

public class FetchScenesTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private SceneListAdapter adapter;
    SwipeRefreshLayout swipe;

    public FetchScenesTask(Activity activity, SceneListAdapter adapter, SwipeRefreshLayout swipe) {
        this.activity = activity;
        this.adapter = adapter;
        this.swipe = swipe;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            adapter.setScenes(RoomDataUtil.getScenes(RestClient.fetchConfigurationDetails()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            adapter.clear();
            adapter.addAll(adapter.getScenes());
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(activity, "Failed to retrieve details.", Toast.LENGTH_LONG).show();
        }
        swipe.setRefreshing(false);
    }
}