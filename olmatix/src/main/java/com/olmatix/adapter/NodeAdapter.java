package com.olmatix.adapter;

/**
 * Created by Lesjaw on 04/12/2016.
 */

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Installed_NodeModel;
import com.olmatix.ui.fragment.Installed_Node;
import com.olmatix.utils.ClickListener;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.OlmatixHolder>  implements ItemTouchHelperAdapter
{

    List<Installed_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;
    private ClickListener clicklistener = null;
    Context context;
    CharSequence textNode;
    CharSequence titleNode;
    String topic;


    public class OlmatixHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fwName, ipAddrs, upTime, siGnal, nodeid,lastAdd;
        public ImageView imgNode, imgStatus;

        public OlmatixHolder(View view) {
            super(view);
            imgNode     = (ImageView) view.findViewById(R.id.icon_node);
            imgStatus   = (ImageView) view.findViewById(R.id.icon_status);
            fwName    = (TextView) view.findViewById(R.id.fw_name);
            ipAddrs     = (TextView) view.findViewById(R.id.ipaddrs);
            siGnal      = (TextView) view.findViewById(R.id.signal);
            upTime      = (TextView) view.findViewById(R.id.uptime);
            nodeid      = (TextView) view.findViewById(R.id.nodeid);
            lastAdd     = (TextView) view.findViewById(R.id.latestAdd);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v, getAdapterPosition());
            }
        }
    }

    public NodeAdapter(List<Installed_NodeModel> nodeList, Context context, OnStartDragListener dragStartListener) {
        this.nodeList = nodeList;
        mDragStartListener = dragStartListener;
        this.context = context;

    }

    @Override
    public OlmatixHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_nodeitemlist, parent, false);

        return new OlmatixHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OlmatixHolder holder, int position) {


        final Installed_NodeModel mInstalledNodeModel = nodeList.get(position);

        if(mInstalledNodeModel.getOnline() != null) {
            if (mInstalledNodeModel.getOnline().equals("true")) {
                holder.imgStatus.setImageResource(R.drawable.ic_check_green);
            } else {
                holder.imgStatus.setImageResource(R.drawable.ic_check_red);
            }
        }
        holder.imgNode.setImageResource(R.drawable.olmatixlogo);
        if(mInstalledNodeModel.getFwName() != null) {
            if (mInstalledNodeModel.getFwName().equals("smartfitting")) {
                holder.imgNode.setImageResource(R.mipmap.ic_light);
            } else if (mInstalledNodeModel.getFwName().equals("smartadapter4ch")) {
                holder.imgNode.setImageResource(R.mipmap.ic_adapter);
            } else if (mInstalledNodeModel.getFwName().equals("smartsensordoor")) {
                holder.imgNode.setImageResource(R.mipmap.door);
            }
        }
        holder.imgStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });


        if(mInstalledNodeModel.getNice_name_n() != null) {
            holder.fwName.setText(mInstalledNodeModel.getNice_name_n());
            titleNode = mInstalledNodeModel.getNice_name_n();
        } else
            holder.fwName.setText(mInstalledNodeModel.getFwName());

        holder.ipAddrs.setText("IP : "+mInstalledNodeModel.getLocalip());
        holder.siGnal.setText("Signal : "+mInstalledNodeModel.getSignal()+"%");
        if (mInstalledNodeModel.getUptime()!=null) {
            long seconds = Long.parseLong(mInstalledNodeModel.getUptime());
            calculateTime(seconds);
            holder.upTime.setText("Uptime : " + calculateTime(seconds));
        }

        if(mInstalledNodeModel.getAdding() != null) {
            holder.nodeid.setText(mInstalledNodeModel.getNodesID());
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(mInstalledNodeModel.getAdding()));
            cal.getTimeInMillis();
            holder.lastAdd.setText("Updated : "+OlmatixUtils.getTimeAgo(cal));
        }
    }

    public static String calculateTime(long seconds) {
        //Log.d("DEBUG", "calculateTime: " + seconds);
        long sec = seconds % 60;
        long minutes = seconds % 3600 / 60;
        long hours = seconds % 86400 / 3600;
        long days = seconds / 86400;
        String uptimeUpd;
        if(days!= 0){
            uptimeUpd = days + "d " + hours + "h";
        } else if (hours != 0){
            uptimeUpd = hours + "h " + minutes + "m";
        } else if (minutes != 0){
            uptimeUpd = minutes + "m " + sec + "s";
        } else {
            uptimeUpd = sec + "s" ;
        }
               // System.out.println(uptimeUpd);
        return uptimeUpd;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    public void removeItem(int position) {
        Installed_Node.dbNodeRepo.deleteNode(nodeList.get(position).getNodesID());

        String mNodeID = nodeList.get(position).getNodesID();
        for (int a=0; a < 10 ;a++) {
            if (a == 0) {topic = "devices/" + mNodeID + "/$online";}
            if (a == 1) {topic = "devices/" + mNodeID + "/$fwname";}
            if (a == 2) {topic = "devices/" + mNodeID + "/$signal";}
            if (a == 3) {topic = "devices/" + mNodeID + "/$uptime";}
            if (a == 4) {topic = "devices/" + mNodeID + "/$name";}
            if (a == 5) {topic = "devices/" + mNodeID + "/$localip";}
            if (a == 6) {topic = "devices/" + mNodeID + "/light/0";}
            if (a == 7) {topic = "devices/" + mNodeID + "/light/1";}
            if (a == 8) {topic = "devices/" + mNodeID + "/light/2";}
            if (a == 9) {topic = "devices/" + mNodeID + "/light/3";}

            try {
                Connection.getClient().unsubscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Log.d("DEBUS", "removeItem: " +a);
        }
        nodeList.remove(position);

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