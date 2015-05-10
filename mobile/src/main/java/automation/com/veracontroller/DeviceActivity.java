package automation.com.veracontroller;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import java.util.HashMap;

import automation.com.veracontroller.async.ToggleBinaryLightTask;
import automation.com.veracontroller.fragments.BinaryLightFragment;
import automation.com.veracontroller.fragments.SceneFragment;
import automation.com.veracontroller.pojo.BinaryLight;

public class DeviceActivity extends FragmentActivity {
    public static PagerAdapter adapterViewPager;

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty

    }

    /**
     *  Binary light click.
     *  
     * @param view
     */
    public void onToggleClicked(View view) {
        Switch aSwitch = ((Switch) view);
        BinaryLight clickedLight = (BinaryLight) view.getTag(R.string.objectHolder);
        Log.i("Light", clickedLight.getName());
        new ToggleBinaryLightTask(view.getContext(), clickedLight, aSwitch.isChecked()).execute();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        adapterViewPager = new DevicePagerActivity(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapterViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DevicePagerActivity extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 2;

        public DevicePagerActivity(FragmentManager fragmentManager) {
            super(fragmentManager);
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
                    return new BinaryLightFragment();
                case 1:
                    return new SceneFragment();
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
}
