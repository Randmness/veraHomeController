package automation.com.veracontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.async.ExecuteSceneTask;
import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.singleton.RoomData;

public class SceneFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeLayout;
    private ListAdapter adapter;
    private View view;

    List<String> scenes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_switch, container, false);
        }

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // new FetchBinaryLightTask(getActivity()).execute();
                        swipeLayout.setRefreshing(false);

                    }
                }, 2000);
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.activity_main_listview);

        if (listView != null && listView.getAdapter() == null) {
            listView.setAdapter(new ArrayAdapter<String>(view.getContext(),
                    android.R.layout.simple_list_item_1, scenes));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //new ExecuteSceneTask(getActivity(), RoomData.getScenes().get(position)).execute();
                }
            });
            adapter = listView.getAdapter();
        }
        return view;
    }

    @Override
    public void onRefresh() {
        view.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);
    }
}
