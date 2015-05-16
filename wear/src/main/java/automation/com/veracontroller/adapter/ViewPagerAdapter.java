package automation.com.veracontroller.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

public class ViewPagerAdapter extends GridPagerAdapter {
    private final Context context;
    private LayoutInflater inflater;
    private List<BinaryLight> lights;
    private List<Scene> scenes;

    public ViewPagerAdapter(final Context context, List<BinaryLight> lights, List<Scene> scenes) {
        this.context = context;
        this.lights = lights;
        this.scenes = scenes;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 2;
    }

    //---Go to current column when scrolling up or down (instead of default column 0)---
    @Override
    public int getCurrentColumnForRow(int row, int currentColumn) {
        return currentColumn;
    }

    @Override
    protected Object instantiateItem(ViewGroup viewGroup, int row, int col) {
        View view;
        if (col == 0){
            view = inflater.inflate(R.layout.view_binary_lights, null);
            WearableListView listView = (WearableListView) view.findViewById(R.id.wearableLightList);
            listView.setAdapter(new BinaryListAdapter(context, lights));
            listView.setGreedyTouchMode(true);
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    BinaryLight light = (BinaryLight) viewHolder.itemView.getTag(R.integer.objectHolder);
                    Log.i("Light clicked: ", light.getName());
                }

                @Override
                public void onTopEmptyRegionClick() {

                }
            });
        } else {
            view = inflater.inflate(R.layout.view_scenes, null);
            WearableListView listView = (WearableListView) view.findViewById(R.id.wearableSceneList);
            listView.setAdapter(new SceneListAdapter(context, scenes));
            listView.setGreedyTouchMode(true);
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    Scene scene = (Scene) viewHolder.itemView.getTag(R.integer.objectHolder);
                    Log.i("Scene clicked: ", scene.getSceneName());
                }

                @Override
                public void onTopEmptyRegionClick() {

                }
            });
        }
        viewGroup.addView(view);
        return view;
    }

    @Override
    protected void destroyItem(ViewGroup viewGroup, int i, int i2, Object o) {
        viewGroup.removeView((View) o);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view.equals(o);
    }
}
