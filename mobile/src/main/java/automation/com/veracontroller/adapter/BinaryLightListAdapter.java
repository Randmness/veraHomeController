package automation.com.veracontroller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.pojo.BinaryLight;

public class BinaryLightListAdapter extends ArrayAdapter<BinaryLight> {
    private final Context context;
    private List<BinaryLight> lights;

    public BinaryLightListAdapter(Context context, List<BinaryLight> lights) {
        super(context, R.layout.row_button, lights);
        this.context = context;
        this.lights = lights;
    }

    public void setList(List<BinaryLight> lights) {
        this.lights = lights;
    }

    public List<BinaryLight> getLights() {
        return this.lights;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BinaryLight light = lights.get(position);
        LightHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_button, null);

            viewHolder = new LightHolder();
            viewHolder.lightText = (TextView) convertView.findViewById(R.id.title);
            viewHolder.roomText = (TextView) convertView.findViewById(R.id.room);
            viewHolder.status = (Switch) convertView.findViewById(R.id.togglebutton);

            convertView.setTag(R.string.viewHolder, viewHolder);
        } else {
            viewHolder = (LightHolder) convertView.getTag(R.string.viewHolder);
        }

        viewHolder.lightText.setText(light.getName() + "\n");
        viewHolder.roomText.setText("   "+light.getRoomName());
        viewHolder.status.setChecked(light.isEnabled());
        viewHolder.status.setTag(R.string.objectHolder, light);

        return convertView;
    }

    static class LightHolder {
        TextView lightText;
        TextView roomText;
        Switch status;
    }
}
