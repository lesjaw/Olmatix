package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Dashboard_NodeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeDashboardAdapter extends RecyclerView.Adapter<NodeDashboardAdapter.ViewHolder> implements ItemTouchHelperAdapter
{

    List<Dashboard_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class ButtonHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status;
        public ImageView imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public class StatusHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status;
        public ImageView imgNode;

        public StatusHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public NodeDashboardAdapter(ArrayList<Dashboard_NodeModel> data, OnStartDragListener dragStartListener) {
        this.nodeList = data;
        mDragStartListener = dragStartListener;

    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public NodeDashboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        final Dashboard_NodeModel mFavoriteModel = nodeList.get(viewType);

        if (mFavoriteModel.getSensor().equals("light")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_dash_button, parent, false);

            return new NodeDashboardAdapter.ButtonHolder(itemView);

        }
        else if(mFavoriteModel.getSensor().equals("close"))
        {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_dash_status, parent, false);

            return new NodeDashboardAdapter.StatusHolder(itemView);
        }

        return null;
    }


    public void onBindViewHolder(final NodeDashboardAdapter.ViewHolder viewHolder, final int position) {
        //final int pos = position;
        final Dashboard_NodeModel mFavoriteModel = nodeList.get(position);
        if(mFavoriteModel.getSensor().equals("ligth")) {
            Log.d("DEBUG", "onBindViewHolder: 1 "+mFavoriteModel.getSensor());

            NodeDashboardAdapter.ButtonHolder holder = (NodeDashboardAdapter.ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            holder.imgNode.setImageResource(R.mipmap.offlamp);



        }else if(mFavoriteModel.getSensor().equals("sensor")) {
            Log.d("DEBUG", "onBindViewHolder: 2 "+mFavoriteModel.getSensor());

            NodeDashboardAdapter.StatusHolder holder = (NodeDashboardAdapter.StatusHolder) viewHolder;
            holder.node_name.setText(mFavoriteModel.getNice_name_d());

            holder.imgNode.setImageResource(R.mipmap.not_armed);


        }


    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(nodeList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }


}
