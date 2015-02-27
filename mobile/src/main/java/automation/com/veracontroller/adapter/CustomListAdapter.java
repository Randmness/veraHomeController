package automation.com.veracontroller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.singleton.RoomData;

public class CustomListAdapter extends ArrayAdapter<String> {
    private final Context context;
    HashMap<String, BinaryLight> map;

    public CustomListAdapter(Context context, List<String> lights, HashMap<String, BinaryLight> map) {
        super(context, R.layout.row_button, lights);
        this.context = context;
        this.map = map;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BinaryLight light = map.get(getItem(position));
        Room room = RoomData.returnRooms().get(light.getRoomNum());

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_button, parent, false);

        TextView lightText = (TextView) rowView.findViewById(R.id.title);
        lightText.setText(light.getName() + "\n");
        TextView roomText = (TextView) rowView.findViewById(R.id.room);
        roomText.setText("Room: " + room.getRoomName());
        Switch status = (Switch) rowView.findViewById(R.id.togglebutton);
        status.setChecked(light.isEnabled());
        status.setTag(light.getDeviceNum());
        status.setTag(Integer.toString(light.getDeviceNum()));

        return rowView;
    }
}
