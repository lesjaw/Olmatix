package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.utils.Connection;

import org.achartengine.tools.Zoom;
import org.appspot.olmatixrtc.ConnectActivity;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        imgCamBut = (ImageButton) findViewById(R.id.camButton);
        textNiceName = (TextView)findViewById(R.id.fwname);
        cv1 = (CardView)findViewById(R.id.cv2);
        setupToolbar();

        Intent i = getIntent();
        node_id = i.getStringExtra("nodeid");
        nicename = i.getStringExtra("nice_name");
        Log.d("DEBUG", "Nodeid & nicename: "+node_id +" "+nicename);

        textNiceName.setText(nicename);

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


            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        double latphone = mPrefHelper.getPhoneLatitude();
        double lngphone = mPrefHelper.getPhoneLongitude();

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latphone, lngphone);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(sydney, 5);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Olmatix App"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

}
