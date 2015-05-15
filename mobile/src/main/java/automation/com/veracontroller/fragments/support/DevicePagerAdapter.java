package automation.com.veracontroller.fragments.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import automation.com.veracontroller.fragments.BinaryLightFragment;
import automation.com.veracontroller.fragments.SceneFragment;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;

public class DevicePagerAdapter extends FragmentStatePagerAdapter {
    private static int NUM_ITEMS = 2;
    private ArrayList<BinaryLight> lights = new ArrayList<>();
    private ArrayList<Scene> scenes = new ArrayList<>();

    public DevicePagerAdapter(FragmentManager fragmentManager, ArrayList<BinaryLight> lights, ArrayList<Scene> scenes) {
        super(fragmentManager);
        this.lights = lights;
        this.scenes = scenes;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return BinaryLightFragment.newInstance(lights);
            case 1:
                return SceneFragment.newInstance(scenes);
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Switches";
            case 1:
                return "Scenes";
            default:
                return "Unknown";
        }
    }
}
