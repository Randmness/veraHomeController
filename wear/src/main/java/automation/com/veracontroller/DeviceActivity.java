package automation.com.veracontroller;

import android.app.Activity;
import android.os.Bundle;

import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends Activity {

    private List<BinaryLight> lights = new ArrayList<>();
    private List<Scene> scenes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lights = getIntent().getParcelableArrayListExtra(IntentConstants.LIGHT_LIST);
        scenes = getIntent().getParcelableArrayListExtra(IntentConstants.SCENE_LIST);
    }


}
