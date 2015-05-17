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
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import automation.com.veracontroller.R;
import automation.com.veracontroller.async.DataLayerThread;
import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

public class ViewPagerAdapter extends GridPagerAdapter {
    private final Context context;
    private LayoutInflater inflater;
    private Gson gson = new Gson();
    private GoogleApiClient googleApiClient;
    private List<BinaryLight> lights;
    private List<Scene> scenes;

    public ViewPagerAdapter(final Context context, List<BinaryLight> lights, List<Scene> scenes, GoogleApiClient googleApiClient) {
        this.context = context;
        this.lights = lights;
        this.scenes = scenes;
        this.googleApiClient = googleApiClient;
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
        WearableListView listView;

        if (col == 0){
            view = inflater.inflate(R.layout.view_binary_lights, null);
            listView = (WearableListView) view.findViewById(R.id.wearableLightList);
            listView.setAdapter(new BinaryListAdapter(context, lights));
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    BinaryLight light = (BinaryLight) viewHolder.itemView.getTag(R.integer.objectHolder);
                    Log.i("Scene clicked: ", light.getName());

                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.LIGHT, gson.toJson(light));
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_DEVICE_LIGHT_TOGGLE.toString(), dataMap, googleApiClient).start();
                }

                @Override
                public void onTopEmptyRegionClick() {}
            });
        } else {
            view = inflater.inflate(R.layout.view_scenes, null);
            listView = (WearableListView) view.findViewById(R.id.wearableSceneList);
            listView.setAdapter(new SceneListAdapter(context, scenes));
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    Scene scene = (Scene) viewHolder.itemView.getTag(R.integer.objectHolder);
                    Log.i("Scene clicked: ", scene.getSceneName());

                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.SCENE, gson.toJson(scene));
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_DEVICE_SCENE_EXECUTION.toString(), dataMap, googleApiClient).start();
                }

                @Override
                public void onTopEmptyRegionClick() {
                    Log.i("Top", "TOP CLICKED");
                }
            });
        }

        final TextView header = (TextView) view.findViewById(R.id.header);
        listView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {}

            @Override
            public void onAbsoluteScrollChange(int i) {
                if (i > 0)
                    header.setY(-i);
            }

            @Override
            public void onScrollStateChanged(int i) {}

            @Override
            public void onCentralPositionChanged(int i) {}
        });

        listView.setGreedyTouchMode(true);
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
