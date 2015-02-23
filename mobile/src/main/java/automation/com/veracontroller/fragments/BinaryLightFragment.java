package automation.com.veracontroller.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import automation.com.veracontroller.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BinaryLightFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BinaryLightFragment extends Fragment {
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static BinaryLightFragment newInstance(int page, String title) {
        BinaryLightFragment fragmentFirst = new BinaryLightFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_binary_light, container, false);
       // TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        //tvLabel.setText(page + " -- " + title);
        return view;
    }

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}