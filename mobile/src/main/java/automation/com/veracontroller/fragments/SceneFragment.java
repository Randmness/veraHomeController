package automation.com.veracontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.R;
import automation.com.veracontroller.adapter.SceneListAdapter;
import automation.com.veracontroller.async.ExecuteSceneTask;
import automation.com.veracontroller.async.FetchScenesTask;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.pojo.Scene;

public class SceneFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeLayout;
    private SceneListAdapter adapter;
    private View view;
    private List<Scene> scenes = new ArrayList<>();

    public static SceneFragment newInstance(ArrayList<Scene> startingScenes) {
        SceneFragment myFragment = new SceneFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(IntentConstants.SCENE_LIST, startingScenes);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            scenes = savedInstanceState.getParcelableArrayList(IntentConstants.SCENE_LIST);
        } else {
            scenes = getArguments().getParcelableArrayList(IntentConstants.SCENE_LIST);
        }

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_switch, container, false);
        }

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                view.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        new FetchScenesTask(getActivity(), adapter, swipeLayout).execute();
                    }
                });
            }
        });

        adapter = new SceneListAdapter(view.getContext(), scenes);
        ListView listView = (ListView) view.findViewById(R.id.activity_main_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Scene clickedScene = (Scene) view.getTag(R.string.objectHolder);
                new ExecuteSceneTask(getActivity(), clickedScene).execute();
            }
        });

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(IntentConstants.SCENE_LIST, (ArrayList) adapter.getScenes());
    }

    public void pollingUpdate(List<Scene> pollScenes) {
        adapter.setScenes(pollScenes);
        adapter.clear();
        adapter.addAll(adapter.getScenes());
        adapter.notifyDataSetChanged();
    }
}
