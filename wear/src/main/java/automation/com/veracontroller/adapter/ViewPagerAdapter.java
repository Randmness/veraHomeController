package automation.com.veracontroller.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

import automation.com.veracontroller.R;
import automation.com.veracontroller.async.DataLayerThread;
import automation.com.veracontroller.async.RequestDataThread;
import automation.com.veracontroller.constants.DataMapConstants;
import automation.com.veracontroller.enums.DataPathEnum;
import automation.com.veracontroller.layout.BinaryLightItemView;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

public class ViewPagerAdapter extends GridPagerAdapter {
    private final Context context;
    private LayoutInflater inflater;
    private Gson gson = new Gson();
    private GoogleApiClient googleApiClient;
    private BinaryListAdapter binaryListAdapter;
    private SceneListAdapter sceneListAdapter;

    private List<BinaryLight> lights;
    private List<Scene> scenes;
    private static final int RESPONSE_TIME_OUT = 10000;

    private ProgressDialog dialog;

    public ViewPagerAdapter(final Context context, List<BinaryLight> lights, List<Scene> scenes,
                            GoogleApiClient googleApiClient, ProgressDialog dialog) {
        this.context = context;
        this.lights = lights;
        this.scenes = scenes;
        this.googleApiClient = googleApiClient;
        this.dialog = dialog;
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
            binaryListAdapter = new BinaryListAdapter(context, lights);
            listView.setAdapter(binaryListAdapter);
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    int position = (Integer) viewHolder.itemView.getTag(R.id.wearableLightList);

                    BinaryLight light = lights.get(position);//(BinaryLight) viewHolder.itemView.getTag(R.integer.objectHolder);
                    dialog.setMessage("Turning "+light.onOrOff(!light.isEnabled())+ " "+ light.getName());
                    dialog.show();
                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.LIGHT, gson.toJson(light));
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_DEVICE_LIGHT_TOGGLE.toString(), dataMap, googleApiClient).start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                                alertDialog.setTitle("Error");
                                alertDialog.setMessage("Communication with system has failed. Double-check "+
                                    "your phone's connectivity and try again.");
                                alertDialog.setPositiveButton("Close",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.create().show();
                            }
                        }
                    }, RESPONSE_TIME_OUT);
                }

                @Override
                public void onTopEmptyRegionClick() {
                    dialog.setMessage("Fetching current details.");
                    dialog.show();
                    new RequestDataThread(DataPathEnum.WEARABLE_CONFIG_DATA_REQUEST,
                            "Requesting update.", googleApiClient).start();
                }
            });
        } else {
            view = inflater.inflate(R.layout.view_scenes, null);
            listView = (WearableListView) view.findViewById(R.id.wearableSceneList);
            sceneListAdapter = new SceneListAdapter(context, scenes);
            listView.setAdapter(sceneListAdapter);
            listView.setClickListener(new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    Scene scene = (Scene) viewHolder.itemView.getTag(R.integer.objectHolder);
                    dialog.setMessage("Executing Scene: "+scene.getSceneName());
                    dialog.show();
                    DataMap dataMap = new DataMap();
                    dataMap.putString(DataMapConstants.SCENE, gson.toJson(scene));
                    dataMap.putString("UUID", UUID.randomUUID().toString());
                    new DataLayerThread(DataPathEnum.WEARABLE_DEVICE_SCENE_EXECUTION.toString(), dataMap, googleApiClient).start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                                alertDialog.setTitle("Error");
                                alertDialog.setMessage("Communication with system has failed. Double-check "+
                                        "your phone's connectivity and try again.");
                                alertDialog.setPositiveButton("Close",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.create().show();
                            }
                        }
                    }, RESPONSE_TIME_OUT);
                }

                @Override
                public void onTopEmptyRegionClick() {
                    dialog.setMessage("Fetching current details.");
                    dialog.show();
                    new RequestDataThread(DataPathEnum.WEARABLE_CONFIG_DATA_REQUEST,
                            "Requesting update.", googleApiClient).start();
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

    public BinaryListAdapter getBinaryListAdapter() {
        return this.binaryListAdapter;
    }

    public SceneListAdapter getSceneListAdapter() {
        return this.sceneListAdapter;
    }

    public void updateLights(List<BinaryLight> newLights) {
        this.lights = newLights;
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
