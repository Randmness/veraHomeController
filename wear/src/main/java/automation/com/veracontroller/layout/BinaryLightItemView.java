package automation.com.veracontroller.layout;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import automation.com.veracontroller.R;

public final class BinaryLightItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {

    private final CircledImageView image;
    private final TextView lightName;
    private final TextView room;

    public BinaryLightItemView(Context context) {
        super(context);
        View.inflate(context, R.layout.row_light, this);
        image = (CircledImageView) findViewById(R.id.image);
        lightName = (TextView) findViewById(R.id.lightName);
        room = (TextView) findViewById(R.id.room);
    }
    @Override
    public void onCenterPosition(boolean b) {
        image.animate().scaleX(1f).scaleY(1f).alpha(1);
        room.animate().scaleX(1f).scaleY(1f).alpha(1);
        lightName.animate().scaleX(1f).scaleY(1f).alpha(1);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        //Animation example to be ran when the view is not the centered one anymore
        image.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        lightName.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        room.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
    }
}
