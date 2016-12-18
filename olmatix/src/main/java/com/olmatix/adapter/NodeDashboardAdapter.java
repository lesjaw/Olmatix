package com.olmatix.adapter;

import android.content.Context;
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
import com.olmatix.ui.fragment.Dashboard_Node;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeDashboardAdapter extends RecyclerView.Adapter<NodeDashboardAdapter.ViewHolder> implements ItemTouchHelperAdapter
{

    List<Dashboard_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;
    Context context;
    String fw_name;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class ButtonHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, upTime, status, fwName;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public class StatusHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, upTime, status, fwName;
        public ImageButton imgNode;

        public StatusHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public NodeDashboardAdapter(List<Dashboard_NodeModel> nodeList, String fw_name, OnStartDragListener dragStartListener, Dashboard_Node dashboard_node) {

        this.nodeList = nodeList;
        mDragStartListener = dragStartListener;
        this.fw_name = fw_name;

        this.context = context;


    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public NodeDashboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;


        if (fw_name.equals("light")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_button, parent, false);

            return new NodeDashboardAdapter.ButtonHolder(itemView);

        }
        else if(fw_name.equals("sensor"))
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
        if(fw_name.equals("ligth")) {

            //Toast.makeText(context,"I m in",Toast.LENGTH_LONG).show();
            final NodeDashboardAdapter.ButtonHolder holder = (NodeDashboardAdapter.ButtonHolder) viewHolder;

            holder.fwName.setText(mFavoriteModel.getFavNodeID());
            holder.imgNode.setImageResource(R.drawable.olmatixlogo);



        }else if(fw_name.equals("sensor"))
        {
            final NodeDashboardAdapter.StatusHolder holder = (NodeDashboardAdapter.StatusHolder) viewHolder;

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
