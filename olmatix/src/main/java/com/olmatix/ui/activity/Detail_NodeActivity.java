package com.olmatix.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.olmatix.adapter.NodeDetailAdapter;
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

        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

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
