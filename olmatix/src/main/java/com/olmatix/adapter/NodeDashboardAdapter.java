package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        public ViewHolder(View v) {super(v);}
    }

    public class ButtonHolder extends ViewHolder {
        public TextView node_name, status;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public class StatusHolder extends ViewHolder {
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;
        final Dashboard_NodeModel mFavoriteModel = nodeList.get(viewType);

        if (mFavoriteModel.getSensor().trim().equals("light")) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_dash_button, viewGroup, false);

            return new ButtonHolder(v);

        }
        else if(mFavoriteModel.getSensor().trim().equals("close"))
        {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_dash_status, viewGroup, false);
            Log.d("DEBUG", "onCreateViewHolder 2: "+mFavoriteModel.getNodeid());

            return new StatusHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //final int pos = position;
       final Dashboard_NodeModel mFavoriteModel = nodeList.get(position);
        Log.d("DEBUG", "onCreateViewHolder 0: "+mFavoriteModel.getSensor());
        Log.d("DEBUG", "onCreateViewHolder 1: "+mFavoriteModel.getNodeid());

        if((mFavoriteModel.getSensor().trim()).equals("light")) {

            final ButtonHolder holder = (ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            if (mFavoriteModel.getStatus().trim().equals("false")) {
                holder.imgNode.setImageResource(R.mipmap.offlamp);
            }else {
                holder.imgNode.setImageResource(R.mipmap.onlamp);
            }


        }else if((mFavoriteModel.getSensor().trim()).equals("close")) {

            final StatusHolder holder = (StatusHolder) viewHolder;
            //final ButtonHolder holder = (ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            if (mFavoriteModel.getStatus().trim().equals("false")) {
                holder.imgNode.setImageResource(R.mipmap.not_armed);
            }else {
                holder.imgNode.setImageResource(R.mipmap.armed);
            }

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
