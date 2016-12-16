package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.service.OlmatixService;
import com.olmatix.ui.fragment.Favorite;
import com.olmatix.ui.fragment.Installed_Node;

/**
 * Created by Lesjaw on 02/12/2016.
 */

public class MainActivity extends AppCompatActivity {

    boolean serverconnected;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    int backButtonCount;
    int flagReceiver=0;
    OrientationEventListener mOrientationListener;
    TabLayout tabLayout;
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get current screen orientation

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

                               tabLayout.addTab(tabLayout.newTab().setText("Dashboard").setIcon(R.drawable.ic_fav));
                               tabLayout.addTab(tabLayout.newTab().setText("Nodes").setIcon(R.drawable.ic_node));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mSwitch_Conn = sharedPref.getBoolean("switch_conn", true);
        Log.d("DEBUG", "SwitchConnPreff: " + mSwitch_Conn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStart() {

        if (flagReceiver == 0) {
            Intent i = new Intent(this, OlmatixService.class);
            startService(i);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatus"));
            flagReceiver = 1;
            Log.d("MainAcitivity = ", "Starting OlmatixService");
        }
            super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("MQTT State");
            //Log.d("receiver", "Status MQTT : " + message);
            if (message==null){
                message = "false";

            }
            if (message.equals("true")){
                serverconnected = true;
                invalidateOptionsMenu();
            } else
                serverconnected = false;
            invalidateOptionsMenu();
        }
    };


    // Override this method to do what you want when the menu is recreated
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (serverconnected) {
            //menu.findItem(R.id.state_conn).setTitle("Connected");
            menu.findItem(R.id.state_conn).setIcon(R.drawable.ic_conn_green);
        } else
            //menu.findItem(R.id.state_conn).setTitle("Not Connected");
            menu.findItem(R.id.state_conn).setIcon(R.drawable.ic_conn_red);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.state_conn) {

            return true;
        }
        if (id == R.id.action_settings) {
            Intent i = new Intent(this,SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_exit) {
            finish();
            System.exit(0);
            return true;
        }

        if (id == R.id.action_about) {
            Intent i = new Intent(this,AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter  {



        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


            switch (position) {
                case 0:
                    //Fragement for Fav Tab
                    return new Favorite();
                case 1:
                    //Fragment for Nodes Tab
                    return new Installed_Node();
            }
            return null;
        }



        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";

            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {

        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        else
        {
            Toast.makeText(this, R.string.backbutton, Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        InitializeUI();
    }

    private void InitializeUI() {
    }
}
