package com.olmatix.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.olmatix.adapter.NodeDashboardAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.groupModel;
import com.olmatix.adapter.groupAdapter;


import java.util.ArrayList;

/**
 * Created by USER on 29/05/2017.
 */

public class DashboardNode extends android.support.v4.app.Fragment {
    private View mView;
    CoordinatorLayout coordinatorLayout;
    private groupModel groupmodel;
    public  static dbNodeRepo mDbNodeRepo;
    private FloatingActionButton mFab;
    private static ArrayList<groupModel> data;
    private static ArrayList<DashboardNodeModel> data1;
    private Context context;
    Context dashboardnode;

    private RecyclerView mRecycleView;
    groupAdapter groupAdapter;
    Context group;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    NodeDashboardAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_dash, container, false);
        coordinatorLayout=(CoordinatorLayout)mView.findViewById(R.id.main_content);

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDbNodeRepo = new dbNodeRepo(getActivity());
        groupmodel = new groupModel();
        data = new ArrayList<>();
        group = getActivity();
        dashboardnode=context;
        setupView();
        onClickListener();


    }

    private void updatelist (){

        groupAdapter.notifyDataSetChanged();
        data.clear();
        data.addAll(mDbNodeRepo.getGroupList());
        if(groupAdapter != null){
            groupAdapter.notifyItemRangeChanged(0, groupAdapter.getItemCount());
        }

        //setRefresh();
    }


    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);
        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mRecycleView.setHasFixedSize(true);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        data.clear();
        data.addAll(mDbNodeRepo.getGroupList());
        groupAdapter = new groupAdapter(data,group,this);
        mRecycleView.setAdapter(groupAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();

            }
        });
    }

    private void setRefresh() {
        data.clear();
        data.addAll(mDbNodeRepo.getGroupList());
        groupAdapter = new groupAdapter(data,group,this);
        mRecycleView.setAdapter(groupAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void onClickListener() {
        mFab.setOnClickListener(mFabClickListener());
    }

    private View.OnClickListener mFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText mEditText = new EditText(getActivity());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Create new group")
                        .setMessage("Type your group name here..")
                        .setView(mEditText)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String inputResult = mEditText.getText().toString();
                                groupmodel.setGroupName(inputResult);
                                mDbNodeRepo.insertgroup(groupmodel);
                                setRefresh();
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
        //Log.d("Receiver ", "Dashboard = Starting..");
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("NotifyChangeDetail");
            if (message==null){
                message = "1";
            }
            if (message.equals("2")){
                updatelist();
            }
            if (message.equals("3")){
                setRefresh();
            }

        }
    };
}
