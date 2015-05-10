package automation.com.veracontroller.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.util.RestClient;

public class ToggleBinaryLightTask extends AsyncTask<Void, Void, Boolean> {
    private BinaryLight light;
    private ProgressDialog dialog;
    private Context context;
    private boolean futureState;

    public ToggleBinaryLightTask(Context context, BinaryLight light, boolean futureState) {
        this.light = light;
        this.context = context;
        this.futureState = futureState;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "Fetching Data",
                "Turning " + light.getName() + " " + light.onOrOff(futureState));
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {
            RestClient.executeSwitchCommand(futureState, light.getDeviceNum());

            //TODO
            //adapter.setLights(RestClient.fetchConfigurationDetails());
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