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
import android.widget.Button;
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

public class groupAdapterNew extends RecyclerView.Adapter<groupAdapterNew.ViewHolder> {

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

    public groupAdapterNew(List<groupModel> nodeList, Context group, DashboardNode dashboardNode) {
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


        return new groupHolder(itemView);

    }


    public class groupHolder extends ViewHolder {
        public Button group_name;
        private FloatingActionButton mFab;


        public groupHolder(View view) {
            super(view);
            group_name = (Button) view.findViewById(R.id.group_name);
            //mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {

        final groupModel mGroupModel = nodeList.get(position);
        final groupHolder holder = (groupHolder) viewHolder;

        holder.group_name.setText(mGroupModel.getGroupName());

//        holder.mFab.setOnClickListener(mFabClickListener());

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            //Log.d("DEBUG", "ViewHolder: ");
        }
    }

}

