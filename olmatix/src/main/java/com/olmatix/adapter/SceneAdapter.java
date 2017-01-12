package com.olmatix.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.SceneModel;

import java.util.ArrayList;

/**
 * Created by Lesjaw on 01/01/2017.
 */

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.SceneHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    ArrayList<SceneModel> nodeList;
    private Animation animConn;
    Context context;
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    public SceneAdapter(ArrayList<SceneModel> data, Context scene_node, OnStartDragListener dragStartListener) {
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

        final SceneModel mSceneModel = nodeList.get(position);

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
        public TextView fwName, ipAddrs, upTime, siGnal, nodeid,lastAdd;

        public SceneHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
