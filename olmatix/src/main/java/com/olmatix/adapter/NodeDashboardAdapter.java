package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    public String nodeType;

    List<Dashboard_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;




    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class ButtonHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, upTime, status, nodeType;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public class StatusHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, upTime, status, nodeType;
        public ImageButton imgNode;

        public StatusHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public NodeDashboardAdapter(ArrayList<Dashboard_NodeModel> data, OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;

    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public NodeDashboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;


        if (nodeType.equals("light")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_button, parent, false);

            return new NodeDashboardAdapter.ButtonHolder(itemView);

        }
        else if(nodeType.equals("sensor"))
        {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor, parent, false);

            return new NodeDashboardAdapter.StatusHolder(itemView);
        }

        return null;
    }


    public void onBindViewHolder(final NodeDashboardAdapter.ViewHolder viewHolder, final int position) {
        //final int pos = position;
        final Dashboard_NodeModel mFavoriteModel = nodeList.get(position);
        if(nodeType.equals("ligth")) {

            //Toast.makeText(context,"I m in",Toast.LENGTH_LONG).show();
            final NodeDashboardAdapter.ButtonHolder holder = (NodeDashboardAdapter.ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            holder.imgNode.setImageResource(R.drawable.olmatixlogo);



        }else if(nodeType.equals("sensor"))
        {
            final NodeDashboardAdapter.StatusHolder holder = (NodeDashboardAdapter.StatusHolder) viewHolder;
            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            holder.imgNode.setImageResource(R.drawable.olmatixlogo);


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
