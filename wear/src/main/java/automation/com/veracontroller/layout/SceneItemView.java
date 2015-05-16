package automation.com.veracontroller.layout;


import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import automation.com.veracontroller.R;

public final class SceneItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {
    private final TextView sceneName;

    public SceneItemView(Context context) {
        super(context);
        View.inflate(context, R.layout.row_scene, this);
        sceneName = (TextView) findViewById(R.id.sceneName);
    }
    @Override
    public void onCenterPosition(boolean b) {
        sceneName.animate().scaleX(1f).scaleY(1f).alpha(1);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        sceneName.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
    }
}
