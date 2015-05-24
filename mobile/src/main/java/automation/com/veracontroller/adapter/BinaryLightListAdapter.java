package automation.com.veracontroller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.pojo.BinaryLight;

public class BinaryLightListAdapter extends ArrayAdapter<BinaryLight> {
    private final Context context;
    private List<BinaryLight> lights;
    private HashMap<String, String> unique = new HashMap<>();

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
        BinaryLight light = this.lights.get(position);
        LightHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_button, null);

            viewHolder = new LightHolder();
            viewHolder.divider = (TextView) convertView.findViewById(R.id.divider);
            viewHolder.lightText = (TextView) convertView.findViewById(R.id.title);
            viewHolder.status = (ToggleButton) convertView.findViewById(R.id.togglebutton);

            convertView.setTag(R.string.viewHolder, viewHolder);
        } else {
            viewHolder = (LightHolder) convertView.getTag(R.string.viewHolder);
        }

        if (position == 0) {
            viewHolder.divider.setVisibility(View.VISIBLE);
            viewHolder.divider.setText(light.getRoomName());
        } else {
            BinaryLight previousLight = this.lights.get(position-1);

            if(previousLight.getRoomName().equalsIgnoreCase(light.getRoomName())) {
                viewHolder.divider.setVisibility(View.GONE);
                viewHolder.divider.setText(light.getRoomName());
            } else {
                viewHolder.divider.setVisibility(View.VISIBLE);
                viewHolder.divider.setText(light.getRoomName());
            }
        }

        viewHolder.lightText.setText(light.getName() + "\n");
        viewHolder.status.setChecked(light.isEnabled());
        viewHolder.status.setTag(R.string.objectHolder, light);
        return convertView;
    }

    static class LightHolder {
        TextView lightText;
        ToggleButton status;
        TextView divider;
    }
}
