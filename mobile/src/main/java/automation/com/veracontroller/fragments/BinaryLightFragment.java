package automation.com.veracontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import automation.com.veracontroller.async.ToggleBinaryLightTask;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Room;
import automation.com.veracontroller.singleton.RoomData;
import android.support.v4.app.FragmentTransaction;
import automation.com.veracontroller.util.RestClient;

public class BinaryLightFragment extends ListFragment {
    private List<Map<String, String>> entries = new ArrayList<Map<String, String>>();
    private HashMap<Integer, BinaryLight> listMapping = new HashMap<Integer, BinaryLight>();
    private ListAdapter adapter;

    private static final String TEXT1_KEY = "lightName";
    private static final String TEXT2_KEY = "location";

    private static final int[] viewIDs = new int[]{
            android.R.id.text1, android.R.id.text2
    };

    private static final String[] keys = new String[]{
            TEXT1_KEY, TEXT2_KEY
    };

    private void generateEntries() {
        HashMap<Integer, Room> rooms = RoomData.returnRooms();
        for (int roomID : rooms.keySet()) {
            Room room = rooms.get(roomID);
            for (BinaryLight light : room.getLights()) {
                HashMap<String, String> entry = new HashMap<String, String>();
                entry.put(TEXT1_KEY, light.getName());
                entry.put(TEXT2_KEY, "Location: " + room.getRoomName() + ", State: " + light.onOrOff());

                listMapping.put(entries.size(), light);
                entries.add(entry);
            }
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        new ToggleBinaryLightTask(view.getContext(), listMapping.get(position)).execute();
        //FragmentTransaction transaction = getFragmentManager().beginTransaction();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        generateEntries();
        setListAdapter(new SimpleAdapter(getActivity(), entries, android.R.layout.simple_list_item_2, keys, viewIDs){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final View renderer = super.getView(position, convertView, parent);
                if (listMapping.get(position).isEnabled())
                {
                    renderer.setBackgroundResource(android.R.color.darker_gray);
                }
                return renderer;
            }
        });
        adapter = getListAdapter();
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}