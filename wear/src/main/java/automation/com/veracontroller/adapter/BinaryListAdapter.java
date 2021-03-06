package automation.com.veracontroller.adapter;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.layout.BinaryLightItemView;
import automation.com.veracontroller.layout.SceneItemView;
import automation.com.veracontroller.pojo.BinaryLight;

public class BinaryListAdapter extends WearableListView.Adapter {
    private Context context;
    private List<BinaryLight> items;

    public BinaryListAdapter(Context context, List<BinaryLight> items) {
        this.context = context;
        this.items = items;
        setHasStableIds(true);
    }

    public void updateLights(List<BinaryLight> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<BinaryLight> getLights() {
        return this.items;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new BinaryLightItemView(context));
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getDeviceNum();
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, final int position) {
        BinaryLightItemView itemView = (BinaryLightItemView) viewHolder.itemView;
        final BinaryLight item = items.get(position);

        TextView lightName = (TextView) itemView.findViewById(R.id.lightName);
        lightName.setText(item.getName());

        TextView room = (TextView) itemView.findViewById(R.id.room);
        room.setText(item.getRoomName());

        final CircledImageView imageView = (CircledImageView) itemView.findViewById(R.id.image);
        if (item.isEnabled()) {
            imageView.setImageResource(R.mipmap.lit_light_bulb);
        } else {
            imageView.setImageResource(R.mipmap.unlit_bulb);
        }
        itemView.setTag(R.integer.objectHolder, item);
        itemView.setTag(R.id.wearableLightList, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
