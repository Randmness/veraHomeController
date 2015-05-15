package automation.com.veracontroller;

import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.async.FetchLocationDetailsTask;
import automation.com.veracontroller.async.ToggleBinaryLightTask;
import automation.com.veracontroller.fragments.BinaryLightFragment;
import automation.com.veracontroller.fragments.SceneFragment;
import automation.com.veracontroller.pojo.BinaryLight;
import automation.com.veracontroller.pojo.Scene;
import automation.com.veracontroller.util.RestClient;

public class DeviceActivity extends FragmentActivity {
    public static final String SCENE_LIST = "SCENE_LIST";
    public static final String LIGHT_LIST = "LIGHT_LIST";

    private List<BinaryLight> lights = new ArrayList<>();
    private List<Scene> scenes = new ArrayList<>();

    public static PagerAdapter adapterViewPager;

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty

    }

    /**
     * Binary light click.
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

        lights = getIntent().getParcelableArrayListExtra(LIGHT_LIST);
        scenes = getIntent().getParcelableArrayListExtra(SCENE_LIST);

        adapterViewPager = new DevicePagerActivity(getSupportFragmentManager(), (ArrayList) lights, (ArrayList) scenes);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (RestClient.getLeverageRemote()) {
            menu.findItem(R.id.enableRemote).setChecked(true);
            menu.findItem(R.id.updateRemoteLogin).setVisible(true);
        } else {
            menu.findItem(R.id.enableRemote).setChecked(false);
            menu.findItem(R.id.updateRemoteLogin).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.updateLocationDetails:
                AlertDialog.Builder webDialog = new AlertDialog.Builder(DeviceActivity.this);
                webDialog.setMessage("Must be connected to wifi.");
                webDialog.setCancelable(true);
                webDialog.setPositiveButton("Update Location Details",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FetchLocationDetailsTask(DeviceActivity.this, false).execute();
                            }
                        });
                webDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertWeb = webDialog.create();
                alertWeb.show();
                break;
            case R.id.sendFeedback:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"test@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Vera Home Controller: Feedback");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Send Email"));
                break;
            case R.id.updateRemoteLogin:
                AlertDialog.Builder loginDialog = new AlertDialog.Builder(DeviceActivity.this);
                loginDialog.setMessage("Must be connected to wifi.");
                loginDialog.setCancelable(false);
                loginDialog.setPositiveButton("Update Credentials",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FetchLocationDetailsTask(DeviceActivity.this, true).execute();
                            }
                        });
                loginDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog loginWeb = loginDialog.create();
                loginWeb.show();
                break;
            case R.id.enableRemote:
                SharedPreferences sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE);
                if (item.isChecked()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("leverageRemote", false);
                    editor.commit();
                    RestClient.setLeverageRemote(false);
                } else {
                    String password = sharedPref.getString("password", null);

                    if (password == null) {
                        AlertDialog.Builder remoteDialog = new AlertDialog.Builder(DeviceActivity.this);
                        remoteDialog.setMessage("Must be connected to wifi.");
                        remoteDialog.setCancelable(false);
                        remoteDialog.setPositiveButton("Update Remote Credentials",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new FetchLocationDetailsTask(DeviceActivity.this, true).execute();
                                    }
                                });
                        remoteDialog.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog remoteWeb = remoteDialog.create();
                        remoteWeb.show();
                    } else {
                        String username = sharedPref.getString("username", null);
                        String serial = sharedPref.getString("serialNumber", null);
                        String remoteUrl = sharedPref.getString("remoteUrl", null);
                        RestClient.setLeverageRemote(true);
                        RestClient.setRemoteURL(remoteUrl);
                        RestClient.updateCredentials(username, password, serial);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("leverageRemote", true);
                        editor.commit();
                    }
                }
                invalidateOptionsMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DevicePagerActivity extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 2;
        private ArrayList<BinaryLight> lights = new ArrayList<>();
        private ArrayList<Scene> scenes = new ArrayList<>();

        public DevicePagerActivity(FragmentManager fragmentManager, ArrayList<BinaryLight>lights, ArrayList<Scene> scenes) {
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
}
