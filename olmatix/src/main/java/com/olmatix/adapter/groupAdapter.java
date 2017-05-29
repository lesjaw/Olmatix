package com.olmatix.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SpinnerObjectDash;
import com.olmatix.model.groupModel;
import com.olmatix.ui.fragment.DashboardNode;
import com.olmatix.utils.GridSpacesItemDecoration;
import com.olmatix.utils.OlmatixUtils;
import com.olmatix.utils.SpinnerListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 29/05/2017.
 */

public class groupAdapter extends RecyclerView.Adapter<groupAdapter.ViewHolder> {

    List<groupModel> nodeList;
    private Context context;
    Context dashboardnode;
    Spinner mSpinner;
    public  static dbNodeRepo mDbNodeRepo;
    private DashboardNodeModel dashboardNodeModel;
    int groupid;
    private RecyclerView mRecycleView;
    NodeDashboardAdapter adapter;
    private static ArrayList<DashboardNodeModel> data;
    int currentview=1;
    int mNoOfColumns;
    int broadRegeister;



    private View.OnClickListener mFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner = new Spinner(context);
                List<SpinnerObjectDash> lables = mDbNodeRepo.getAllLabelsDash();
                ArrayAdapter<SpinnerObjectDash> dataAdapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item,lables);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);
                new AlertDialog.Builder(context)
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

                                Log.d("DEBUG", "onClickGroup: "+groupid);
                                dashboardNodeModel.setGroupid(String.valueOf(groupid));
                                mDbNodeRepo.insertFavNode(dashboardNodeModel);
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

            }
        };
    }

    public groupAdapter(List<groupModel> nodeList, Context group, DashboardNode dashboardNode) {
        this.nodeList = nodeList;
        this.context = group;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_dash, parent, false);

        mRecycleView    = (RecyclerView) itemView.findViewById(R.id.rv);

        mDbNodeRepo = new dbNodeRepo(context);
        dashboardnode=context;
        dashboardNodeModel= new DashboardNodeModel();
        data = new ArrayList<>();

        mRecycleView.setHasFixedSize(true);

        final PreferenceHelper mPrefHelper = new PreferenceHelper(context.getApplicationContext());
        int currentOrientation = context.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            float colom = mPrefHelper.getLength();
            mNoOfColumns = Math.round(colom);
        }
        else {
            float colomw = mPrefHelper.getWidht();
            mNoOfColumns = Math.round(colomw);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(context,mNoOfColumns);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.addItemDecoration(new GridSpacesItemDecoration(OlmatixUtils.dpToPx(2),true));

        mRecycleView.setItemAnimator(new DefaultItemAnimator());
            LocalBroadcastManager.getInstance(context).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatusDetail"));

        return new groupHolder(itemView);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("NotifyChangeDetail");

            if (message==null){
                message = "1";
            }
            if (message.equals("2")){
                setRefresh();
                adapter.notifyChange();
            }
        }
    };


    public class groupHolder extends ViewHolder {
        public TextView group_name;
        private FloatingActionButton mFab;


        public groupHolder(View view) {
            super(view);
            group_name = (TextView) view.findViewById(R.id.group_name);
            mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);

        final groupModel mGroupModel = nodeList.get(position);
        final groupHolder holder = (groupHolder) viewHolder;

        holder.group_name.setText(mGroupModel.getGroupName());
        groupid = mGroupModel.getGroupid();
        Log.d("DEBUG", "onBindViewHolder: "+groupid);
        data.clear();
        data.addAll(mDbNodeRepo.getNodeDetailDashNew(String.valueOf(groupid)));
        adapter = new NodeDashboardAdapter(data, dashboardnode, this);
        mRecycleView.setAdapter(adapter);

        holder.mFab.setOnClickListener(mFabClickListener());

    }


    private void setRefresh() {

        adapter.notifyDataSetChanged();
        data.clear();
        data.addAll(mDbNodeRepo.getNodeDetailDashNew(String.valueOf(1)));
        if(adapter != null){
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            Log.d("DEBUG", "Group setRefresh: ");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            //Log.d("DEBUG", "ViewHolder: ");
        }
    }

}

