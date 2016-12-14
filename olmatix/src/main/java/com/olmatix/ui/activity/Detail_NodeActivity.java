package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.olmatix.adapter.NodeDetailAdapter;
import com.olmatix.adapter.OlmatixAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;

/**
 * Created by android on 12/13/2016.
 */

public class Detail_NodeActivity extends AppCompatActivity {

    dbNodeRepo dbNodeRepo;
    String node_id;
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager layoutManager;
    NodeDetailAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int flagReceiver=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_node);

        Intent i = getIntent();
        node_id = i.getStringExtra("node_id");

        dbNodeRepo =new dbNodeRepo(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupView();


    }

    private void setupView() {
        mRecycleView    = (RecyclerView) findViewById(R.id.rv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        adapter = new NodeDetailAdapter(dbNodeRepo.getNodeDetailID(node_id));
        mRecycleView.setAdapter(adapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();



            }
        });

    }
    private void setRefresh() {

        adapter = new NodeDetailAdapter(dbNodeRepo.getNodeDetailID(node_id));
        mRecycleView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onStart() {

        if (flagReceiver==0) {
            /*Intent i = new Intent(getActivity(), OlmatixService.class);
            getActivity().startService(i);*/

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatus"));
            Log.d("Receiver ", "Installed_Node = Starting..");
            flagReceiver = 1;
        }
        super.onStart();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("NotifyChange");
            //Log.d("receiver", "NotifyChange : " + message);
            if (message==null){
                message = "false";

            }
            if (message.equals("true")){
                updatelist();

            }
        }
    };

    private void updatelist (){
        adapter.notifyDataSetChanged();
        adapter = new NodeDetailAdapter(dbNodeRepo.getNodeDetailID(node_id));
        mRecycleView.setAdapter(adapter);
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
}
