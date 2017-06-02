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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.olmatix.adapter.NodeDashboardAdapter;
import com.olmatix.adapter.groupAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.SpinnerObjectDash;
import com.olmatix.model.groupModel;
import com.olmatix.utils.GridSpacesItemDecoration;
import com.olmatix.utils.OlmatixUtils;
import com.olmatix.utils.SpinnerListener;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 29/05/2017.
 */

public class DashboardNode extends android.support.v4.app.Fragment {
    private View mView;
    CoordinatorLayout coordinatorLayout;
    private groupModel groupmodel;
    public  static dbNodeRepo mDbNodeRepo;
    private FloatingActionButton mFab,mFab1;
    private static ArrayList<groupModel> data;
    private static ArrayList<DashboardNodeModel> data1;
    private Context context;
    Context dashboardnode;
    int mNoOfColumns;
    private RecyclerView mRecycleView,mRecycleView1;
    com.olmatix.adapter.groupAdapter groupAdapter;
    Context group;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    NodeDashboardAdapter adapter;
    String currentgroupid;
    Spinner mSpinner;
    private DashboardNodeModel dashboardNodeModel;



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

        group = getActivity();
        dashboardnode=getActivity();

        data = new ArrayList<>();
        data1 = new ArrayList<>();
        dashboardNodeModel= new DashboardNodeModel();

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

        adapter.notifyDataSetChanged();
        data1.clear();
        data1.addAll(mDbNodeRepo.getNodeDetailDashNew(String.valueOf(currentgroupid)));
        if(adapter != null){
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
        //setRefresh();
    }


    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mRecycleView1    = (RecyclerView) mView.findViewById(R.id.rv1);

        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);
        mFab1            = (FloatingActionButton) mView.findViewById(R.id.fab1);

        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mRecycleView.setHasFixedSize(true);
        mRecycleView1.setHasFixedSize(true);

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getActivity().getApplicationContext());
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            float colom = mPrefHelper.getLength();
            mNoOfColumns = Math.round(colom);

        }
        else {
            float colomw = mPrefHelper.getWidht();
            mNoOfColumns = Math.round(colomw);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),mNoOfColumns);

        mRecycleView1.setLayoutManager(layoutManager);
        mRecycleView1.addItemDecoration(new GridSpacesItemDecoration(OlmatixUtils.dpToPx(4),true));

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(horizontalLayoutManagaer);

        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView1.setItemAnimator(new DefaultItemAnimator());

        currentgroupid="1";

        data1.clear();
        data1.addAll(mDbNodeRepo.getNodeDetailDashNew(currentgroupid));
        adapter = new NodeDashboardAdapter(data1, dashboardnode, this);
        mRecycleView1.setAdapter(adapter);

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

    private void setRefresh1(String groupid) {
        data1.clear();
        data1.addAll(mDbNodeRepo.getNodeDetailDashNew(groupid));
        adapter = new NodeDashboardAdapter(data1, dashboardnode, this);
        mRecycleView1.setAdapter(adapter);
    }

    private void onClickListener() {
        mFab.setOnClickListener(mFabClickListener());
        mFab1.setOnClickListener(mFab1ClickListener());

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
    private View.OnClickListener mFab1ClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner = new Spinner(dashboardnode);
                List<SpinnerObjectDash> lables = mDbNodeRepo.getAllLabelsDash();
                ArrayAdapter<SpinnerObjectDash> dataAdapter = new ArrayAdapter<>(dashboardnode,
                        android.R.layout.simple_spinner_item,lables);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);
                new AlertDialog.Builder(dashboardnode)
                        .setTitle("Add Node")
                        .setMessage("Please choose your existing Nodes!")
                        .setView(mSpinner)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSpinner.setOnItemSelectedListener(new SpinnerListener());
                                int databaseId = Integer.parseInt (String.valueOf(( (SpinnerObjectDash) mSpinner.getSelectedItem ()).getId()));
                                // Log.d("DEBUG", "onClick: "+String.valueOf(databaseId));
                                dashboardNodeModel.setNice_name_d(String.valueOf(databaseId));
                                dashboardNodeModel.setGroupid(String.valueOf(currentgroupid));
                                mDbNodeRepo.insertFavNode(dashboardNodeModel);
                                updatelist();
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("groupid"));
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

            String groupid = intent.getStringExtra("groupid");
            if (groupid!=null) {
                setRefresh1(groupid);
                currentgroupid=groupid;
            }
        }
    };
}
