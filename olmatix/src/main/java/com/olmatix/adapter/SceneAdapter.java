package com.olmatix.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.AllSceneModel;
import com.olmatix.model.SceneModel;
import com.olmatix.ui.activity.scene.ScheduleActivity;

import java.util.ArrayList;

/**
 * Created by Lesjaw on 01/01/2017.
 */

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.SceneHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    ArrayList<AllSceneModel> nodeList;
    private Animation animConn;
    Context context;
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    public SceneAdapter(ArrayList<AllSceneModel> data, Context scene_node, OnStartDragListener dragStartListener) {
        this.nodeList = data;
        mDragStartListener = dragStartListener;
        this.context = scene_node;

    }


    @Override
    public SceneHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_scene_item, parent, false);

        return new SceneHolder(itemView);
    }

    @Override
    public void onBindViewHolder( final SceneHolder holder, int position) {

        final AllSceneModel mSceneModel = nodeList.get(position);
        holder.fwName.setText(mSceneModel.getSceneName());
        if(mSceneModel.getSceneType() == 0)
        {
            holder.fwType.setText("Time Schedule");
            holder.icon_node.setImageResource(R.drawable.schedule);

        }
        else if(mSceneModel.getSceneType() == 1)
        {
            holder.fwType.setText("Base on Location");
            holder.icon_node.setImageResource(R.drawable.location);


        }
        else if(mSceneModel.getSceneType() == 2)
        {
            holder.fwType.setText("Base on Sensor");
            holder.icon_node.setImageResource(R.drawable.sensor);

        }
        else if(mSceneModel.getSceneType() == 3)
        {
            holder.fwType.setText("Self Trigger");
            holder.icon_node.setImageResource(R.drawable.selftrigger);

        }

    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class SceneHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fwName,fwType;
        ImageView icon_node;

        public SceneHolder(View itemView) {
            super(itemView);

            fwName = (TextView) itemView.findViewById(R.id.fw_name);
            fwType = (TextView) itemView.findViewById(R.id.fwType);
            icon_node = (ImageView) itemView.findViewById(R.id.icon_node);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                Context c = itemView.getContext();
                Intent intent = new Intent(c, ScheduleActivity.class);
                intent.putExtra("SCENETYPE", nodeList.get(getAdapterPosition()).getSceneName());
                intent.putExtra("SCENEID", nodeList.get(getAdapterPosition()).getSceneType());

                c.startActivity(intent);
            }



        }
    }
}
