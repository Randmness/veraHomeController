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

import automation.com.veracontroller.R;
import automation.com.veracontroller.async.ExecuteSceneTask;
import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.singleton.RoomData;

public class SceneFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListAdapter adapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_switch, container, false);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new FetchBinaryLightTask(getActivity()).execute();
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                }, 2000);
            }
        });

        mListView = (ListView) view.findViewById(R.id.activity_main_listview);

        if (mListView != null && mListView.getAdapter() == null) {
            mListView.setAdapter(new ArrayAdapter<String>(view.getContext(),
                    android.R.layout.simple_list_item_1, RoomData.getSceneNames()));

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new ExecuteSceneTask(getActivity(), RoomData.getScenes().get(position)).execute();
                }
            });
            adapter = mListView.getAdapter();
        }
        return view;
    }

    @Override
    public void onRefresh() {
        view.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }
}
