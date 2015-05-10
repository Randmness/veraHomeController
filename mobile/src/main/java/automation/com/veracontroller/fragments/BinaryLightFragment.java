package automation.com.veracontroller.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.adapter.BinaryLightListAdapter;
import automation.com.veracontroller.async.FetchBinaryLightTask;
import automation.com.veracontroller.pojo.BinaryLight;

public class BinaryLightFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeLayout;
    private BinaryLightListAdapter adapter;
    private View view;

    private List<BinaryLight> lights = new ArrayList<>();
    private static final String BINARY_LIGHTS = "BINARY_LIGHTS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            lights = savedInstanceState.getParcelableArrayList(BINARY_LIGHTS);
        }


        view = inflater.inflate(R.layout.fragment_binary_light, container, false);
        adapter = new BinaryLightListAdapter(view.getContext(), lights);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container2);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        new FetchBinaryLightTask(getActivity(), adapter, swipeLayout).execute();
                    }
                });
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.activity_main_listview2);
        listView.setAdapter(adapter);

        if (savedInstanceState == null) {
            swipeLayout.post(new Runnable() {
                @Override public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    new FetchBinaryLightTask(getActivity(), adapter, swipeLayout).execute();
                }
            });
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BINARY_LIGHTS, (ArrayList) adapter.getLights());
    }

}
