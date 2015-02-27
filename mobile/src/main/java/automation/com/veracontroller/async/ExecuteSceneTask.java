package automation.com.veracontroller.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.singleton.RoomData;
import automation.com.veracontroller.util.RestClient;

public class ExecuteSceneTask extends AsyncTask<Void, Void, Boolean> {
    private Scene scene;
    private ProgressDialog dialog;
    private Context context;

    public ExecuteSceneTask(Context context, Scene scene) {
        this.scene = scene;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "Executing Scene",
                scene.getSceneName());
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            RestClient.executeSceneCommand(scene.getSceneNum());
            //RoomData.resetMap(RestClient.fetchConfigurationDetails());
        } catch (Exception e) {
            Log.e("Failure", "Failed to execute command.");
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
        }
        dialog.dismiss();
    }
}