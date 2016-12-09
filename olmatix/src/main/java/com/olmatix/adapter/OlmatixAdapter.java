package com.olmatix.adapter;

/**
 * Created by Lesjaw on 04/12/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.model.NodeModel;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.fragment.Installed_Node;

import java.util.Collections;
import java.util.List;

public class OlmatixAdapter extends RecyclerView.Adapter<OlmatixAdapter.OlmatixHolder>  implements ItemTouchHelperAdapter {

    List<NodeModel> nodeList;



    public class OlmatixHolder extends RecyclerView.ViewHolder {
        public TextView fwName, ipAddrs, upTime, siGnal;
        public ImageView imgNode, imgStatus;

        public OlmatixHolder(View view) {
            super(view);
            imgNode     = (ImageView) view.findViewById(R.id.icon_node);
            imgStatus   = (ImageView) view.findViewById(R.id.icon_status);
            fwName    = (TextView) view.findViewById(R.id.fw_name);
            ipAddrs     = (TextView) view.findViewById(R.id.ipaddrs);
            siGnal      = (TextView) view.findViewById(R.id.signal);
            upTime      = (TextView) view.findViewById(R.id.uptime);
        }
    }

    public OlmatixAdapter(List<NodeModel> nodeList) {
        this.nodeList = nodeList;
    }



    @Override
    public OlmatixHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_nodeitemlist, parent, false);

        return new OlmatixHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OlmatixHolder holder, int position) {

        final NodeModel mNodeModel = nodeList.get(position);
        if(mNodeModel.getOnline().equals("true")){
            holder.imgStatus.setImageResource(R.drawable.ic_node_online);
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_node_offline);
        }
        holder.imgNode.setImageResource(R.drawable.olmatixlogo);
        holder.fwName.setText(mNodeModel.getFwName());
        holder.ipAddrs.setText(mNodeModel.getLocalip());
        holder.siGnal.setText(mNodeModel.getSignal());
        holder.upTime.setText(mNodeModel.getUptime());
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    public void removeItem(int position) {
        nodeList.remove(position);
        Installed_Node.dbNodeRepo.delete(nodeList.get(position).getNid());
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, nodeList.size());
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