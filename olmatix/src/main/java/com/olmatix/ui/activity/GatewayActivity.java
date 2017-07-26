package com.olmatix.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.adapter.CCTVadapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.CCTVModel;

import java.util.ArrayList;

public class GatewayActivity extends AppCompatActivity {
    private String node_id, nicename;
    LinearLayout coordinatorLayout;
    private CCTVModel cctvModel;
    public static dbNodeRepo dbNodeRepo;
    private static ArrayList<CCTVModel> data;
    private Activity mActivity;
    CCTVadapter adapter;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mChange = intent.getStringExtra("imagesUrl");
            //Log.d(TAG, "onReceive: ");
            if (mChange==null){

            } else {
               adapter.notifyDataSetChanged();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);


        mActivity = this;

        data = new ArrayList<>();
        dbNodeRepo = new dbNodeRepo(this);
        cctvModel = new CCTVModel();
        setupToolbar();


        Intent i = getIntent();
        node_id = i.getStringExtra("nodeid");
        nicename = i.getStringExtra("nice_name");
        Log.d("DEBUG", "Nodeid & nicename: "+node_id +" "+nicename);
        setupView();
    }

    private void setupView(){
        coordinatorLayout=(LinearLayout)findViewById(R.id.main_content);
        CardView btn_crv = (CardView) findViewById(R.id.cv1);
        GridView list_IPcam = (GridView) findViewById(R.id.grid);
        //ImageButton btn_config = (ImageButton) findViewById(R.id.btn_config);

        data.clear();
        data.addAll(dbNodeRepo.getIPcamList(node_id));
        int countDB = dbNodeRepo.getIPcamList(node_id).size();
        Log.d("DEBUG", "setupView: "+countDB);
        adapter = new CCTVadapter(data, this);
        list_IPcam.setAdapter(adapter);
        if (countDB>1) {
            list_IPcam.setNumColumns(2);
        } else {
            list_IPcam.setNumColumns(1);
        }

        btn_crv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText address = new EditText(GatewayActivity.this);
                address.setHint("RTSP IP address");
                final EditText label = new EditText(GatewayActivity.this);
                label.setHint("CCTV Name label");
                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(address);
                layout.addView(label);
                new AlertDialog.Builder(GatewayActivity.this)
                        .setTitle("Add CCTV")
                        .setMessage("Please type RTSP address of your CCTV! and Name of it")
                        .setView(layout)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String input = address.getText().toString();
                                String labelcctv = label.getText().toString();
                                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"IP Address "+input +" name : "+labelcctv
                                        , TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.show();
                                cctvModel.setNodeId(node_id);
                                cctvModel.setIp(input);
                                cctvModel.setName(labelcctv);
                                dbNodeRepo.insertCCTV(cctvModel);

                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
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
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("cctvadapter"));
    }
}
