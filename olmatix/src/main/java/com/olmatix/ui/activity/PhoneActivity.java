package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import org.achartengine.tools.Zoom;
import org.appspot.olmatixrtc.ConnectActivity;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by USER on 02/06/2017.
 */

public class PhoneActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageButton imgCamBut;
    TextView textNiceName;
    CardView cv1;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    private String node_id, nicename;
    private GoogleMap mMap;
    ArrayList<InstalledNodeModel> data;
    public static dbNodeRepo mDbNodeRepo;
    String loc,locYou = null;
    String adString, adStringYou = "";
    private String Distance;
    String loc1 = null;
    double latphone = 0;
    double lngphone = 0;
    String jarak;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        data = new ArrayList<>();
        mDbNodeRepo = new dbNodeRepo(getApplicationContext());



        imgCamBut = (ImageButton) findViewById(R.id.camButton);
        textNiceName = (TextView)findViewById(R.id.fwname);
        cv1 = (CardView)findViewById(R.id.cv2);
        setupToolbar();

        Intent i = getIntent();
        node_id = i.getStringExtra("nodeid");
        nicename = i.getStringExtra("nice_name");
        Log.d("DEBUG", "Nodeid & nicename: "+node_id +" "+nicename);

        textNiceName.setText(nicename);

        data.clear();
        data.addAll(mDbNodeRepo.getNodeListbyNode(node_id));
        int countDB = mDbNodeRepo.getNodeListbyNode(node_id).size();
        if (countDB != 0) {
            for (int z = 0; z < countDB; z++) {
                loc1 = data.get(z).getOta();
                if (loc1!=null) {
                    String[] outputDevices = loc1.split(",");
                    latphone = Double.parseDouble(String.valueOf(outputDevices[0]));
                    lngphone = Double.parseDouble(String.valueOf(outputDevices[1]));
                }
            }
        }

        cv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                Random r = new Random();
                int i1 = r.nextInt(80 - 65) + 65;
                if (mStatusServer) {

                    String topic = "devices/" + node_id + "/$calling";
                    String payload = "true-"+i1;
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setQos(1);
                        message.setRetained(true);
                        Connection.getClient().publish(topic, message);

                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("addNode");
                    intent.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                Intent i = new Intent(getBaseContext(), ConnectActivity.class);
                i.putExtra("node_id", node_id+i1);
                startActivity(i);
            }
        });


    }

    private void setupToolbar(){
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>OLMATIX </font>"));

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon action bar is clicked; go to parent activity
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("Location"));
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mChange = intent.getStringExtra("latlng");
            //Log.d(TAG, "onReceive: ");
            if (mChange!=null){
                loc1 = mChange;
                String[] outputDevices = loc1.split(",");
                latphone = Double.parseDouble(String.valueOf(outputDevices[0]));
                lngphone = Double.parseDouble(String.valueOf(outputDevices[1]));

                mMap.clear();
                onMapReady(mMap);
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> list;
            list = geocoder.getFromLocation(latphone, lngphone, 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                loc = address.getLocality();

                if (address.getAddressLine(0) != null)
                    adString = ", " + address.getAddressLine(0);

            }

        } catch (final IOException e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("DEBUG", "Geocoder ERROR", e);
                }
            }).start();
            loc = OlmatixUtils.gpsDecimalFormat.format(latphone) + " : " + OlmatixUtils.gpsDecimalFormat.format(lngphone);

        }



        final float[] res = new float[3];
        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        Location.distanceBetween(latphone, lngphone, mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), res);

        double latyou = mPrefHelper.getPhoneLatitude();
        double lngyou = mPrefHelper.getPhoneLongitude();

        try {
            List<Address> list1;
            list1 = geocoder.getFromLocation(latyou, lngyou, 1);
            if (list1 != null && list1.size() > 0) {
                Address address1 = list1.get(0);
                locYou = address1.getLocality();

                if (address1.getAddressLine(0) != null)
                    adStringYou = ", " + address1.getAddressLine(0);

            }

        } catch (final IOException e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("DEBUG", "Geocoder ERROR", e);
                }
            }).start();
            //loc = OlmatixUtils.gpsDecimalFormat.format(latphone) + " : " + OlmatixUtils.gpsDecimalFormat.format(lngphone);

        }

        if (mPrefHelper.getHomeLatitude() != 0) {

            String unit = " m";
            if (res[0] > 2000) {// uuse km
                unit = " km";
                res[0] = res[0] / 1000;

            }
            Distance = loc +adString;
            jarak = "it's "+ (int) res[0] + unit ;
            Log.d("DEBUG", "Distance SERVICE 1: " + Distance);

            LatLng sydney = new LatLng(latphone, lngphone);
            LatLng you = new LatLng(latyou, lngyou);

            Marker markerYou = mMap.addMarker(new MarkerOptions().position(you).title("This is you").snippet(
                    locYou+adStringYou+"\n"+loc1)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            //markerYou.remove();
            markerYou.showInfoWindow();

            Marker marker = mMap.addMarker(new MarkerOptions().position(sydney).title(node_id).snippet(
                    Distance+"\n"+jarak+" from your location"+"\n"+loc1)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            marker.showInfoWindow();

            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                    LinearLayout info = new LinearLayout(context);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(context);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.LEFT);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(context);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }
    }
}
