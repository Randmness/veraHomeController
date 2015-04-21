package automation.com.veracontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import automation.com.veracontroller.R;
import automation.com.veracontroller.adapter.CustomListAdapter;
import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.singleton.RoomData;

public class BinaryLightFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListAdapter adapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_binary_light, container, false);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container2);
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

        mListView = (ListView) view.findViewById(R.id.activity_main_listview2);
        if (mListView != null && mListView.getAdapter() == null) {
            mListView.setAdapter(new CustomListAdapter(view.getContext(), RoomData.getLightIDs(), RoomData.getLightMap()));
        }

        adapter = mListView.getAdapter();

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
