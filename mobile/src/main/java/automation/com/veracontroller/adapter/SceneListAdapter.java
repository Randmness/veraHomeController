package automation.com.veracontroller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.pojo.Scene;

public class SceneListAdapter extends ArrayAdapter<Scene> {
    private final Context context;
    private List<Scene> scenes;

    public SceneListAdapter(Context context, List<Scene> scenes) {
        super(context, R.layout.scene_row, scenes);
        this.context = context;
        this.scenes = scenes;
    }

    public List<Scene> getScenes() {
        return this.scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Scene scene = scenes.get(position);
        SceneHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.scene_row, null);

            viewHolder = new SceneHolder();
            viewHolder.sceneText = (TextView) convertView.findViewById(R.id.scene_name);

            convertView.setTag(R.string.viewHolder, viewHolder);
        } else {
            viewHolder = (SceneHolder) convertView.getTag(R.string.viewHolder);
        }

        viewHolder.sceneText.setText(scene.getSceneName() + "\n");
        convertView.setTag(R.string.objectHolder, scene);

        return convertView;
    }

    static class SceneHolder {
        TextView sceneText;
    }
}