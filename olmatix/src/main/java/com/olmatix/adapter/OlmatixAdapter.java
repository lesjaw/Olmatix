package com.olmatix.adapter;

/**
 * Created by Lesjaw on 04/12/2016.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.model.NodeModel;
import com.olmatix.lesjaw.olmatix.R;

import java.util.List;

public class OlmatixAdapter extends RecyclerView.Adapter<OlmatixAdapter.OlmatixHolder> {

    List<NodeModel> nodeList;
    dbNodeRepo db;


    public class OlmatixHolder extends RecyclerView.ViewHolder {
        public TextView nodeName, ipAddrs, upTime;
        public ImageView imgNode, imgStatus;

        public OlmatixHolder(View view) {
            super(view);
            imgNode     = (ImageView) view.findViewById(R.id.icon_node);
            imgStatus   = (ImageView) view.findViewById(R.id.icon_status);
            nodeName    = (TextView) view.findViewById(R.id.node_name);
            ipAddrs     = (TextView) view.findViewById(R.id.ipaddrs);
            upTime      = (TextView) view.findViewById(R.id.uptime);
        }
    }

    public OlmatixAdapter(List<NodeModel> nodeList) {
        this.nodeList = nodeList;
    }



    @Override
    public OlmatixHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nodeitemlist, parent, false);

        return new OlmatixHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OlmatixHolder holder, int position) {

        final NodeModel mNodeModel = nodeList.get(position);
        if(mNodeModel.getOnline().equals("1")){
            holder.imgNode.setImageResource(R.drawable.ic_node_online);
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_node_offline);
        }


        holder.nodeName.setText(mNodeModel.getNiceName());
        holder.ipAddrs.setText(mNodeModel.getLocalip());
        holder.upTime.setText(mNodeModel.getUptime());
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
