package com.olmatix.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.groupModel;
import com.olmatix.ui.fragment.DashboardNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 29/05/2017.
 */

public class groupAdapter extends RecyclerView.Adapter<groupAdapter.ViewHolder> {

    List<groupModel> nodeList;
    private Context context;
    public  static dbNodeRepo mDbNodeRepo;
    NodeDashboardAdapter adapter;
    private final ArrayList<Integer> selected = new ArrayList<>();

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


        mDbNodeRepo = new dbNodeRepo(context);

        return new groupHolder(itemView);

    }


    public class groupHolder extends ViewHolder {
        public Button group_name;


        public groupHolder(View view) {
            super(view);
            group_name = (Button) view.findViewById(R.id.group_name);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {

        final groupModel mGroupModel = nodeList.get(position);
        final groupHolder holder = (groupHolder) viewHolder;

        if (!selected.contains(position)){
            // view not selected
            holder.group_name.setEnabled(true);
        }
        else
            // view is selected
            holder.group_name.setEnabled(false);

        holder.group_name.setText(mGroupModel.getGroupName());

        holder.group_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("groupid");
                intent.putExtra("groupid", String.valueOf(mGroupModel.getGroupid()));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                holder.group_name.setEnabled(false);

                if (selected.isEmpty()){
                    selected.add(position);
                }else {
                    int oldSelected = selected.get(0);
                    selected.clear();
                    selected.add(position);
                    // we do not notify that an item has been selected
                    // because that work is done here.  we instead send
                    // notifications for items to be deselected
                    notifyItemChanged(oldSelected);
                }
            }

        });

        holder.group_name.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete this Node?");
                builder.setMessage(mGroupModel.getGroupName());

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDbNodeRepo.deleteGroup(String.valueOf(mGroupModel.getGroupid()));
                        Intent intent = new Intent("MQTTStatusDetail");
                        intent.putExtra("NotifyChangeDetail", String.valueOf(2));
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


                return false;
            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            //Log.d("DEBUG", "ViewHolder: ");
        }
    }

}

