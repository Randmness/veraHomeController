package automation.com.veracontroller.adapter;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.layout.SceneItemView;
import automation.com.veracontroller.pojo.Scene;

public class SceneListAdapter extends WearableListView.Adapter {
    private Context context;
    private List<Scene> sceneItems;

    public SceneListAdapter(Context context, List<Scene> sceneItems) {
        this.context = context;
        this.sceneItems = sceneItems;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new SceneItemView(context));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, final int position) {
        final Scene item = sceneItems.get(position);

        SceneItemView itemView = (SceneItemView) viewHolder.itemView;
        TextView sceneName = (TextView) itemView.findViewById(R.id.sceneName);
        sceneName.setText(item.getSceneName());
        itemView.setTag(R.integer.objectHolder, item);
    }

    @Override
    public int getItemCount() {
        return sceneItems.size();
    }
}
