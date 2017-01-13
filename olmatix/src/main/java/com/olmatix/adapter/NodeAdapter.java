package com.olmatix.adapter;

/**
 * Created by Lesjaw on 04/12/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.fragment.InstalledNode;
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

    List<InstalledNodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;
    private ClickListener clicklistener = null;
    Context context;
    CharSequence textNode;
    CharSequence titleNode;
    String topic;
    dbNodeRepo dbNodeRepo;


    class OlmatixHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView fwName, ipAddrs, upTime, siGnal, nodeid,lastAdd;
        ImageView imgNode, imgStatus;
        ImageButton imgBut;

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
            imgBut      = (ImageButton) view.findViewById(R.id.opt);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v, getAdapterPosition());
            }
        }
    }

    public NodeAdapter(List<InstalledNodeModel> nodeList, Context context, OnStartDragListener dragStartListener) {
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
    public void onBindViewHolder(final OlmatixHolder holder, final int position) {

        final InstalledNodeModel mInstalledNodeModel = nodeList.get(position);
        dbNodeRepo = new dbNodeRepo(context);


        if(mInstalledNodeModel.getOnline() != null) {
            if (mInstalledNodeModel.getOnline().equals("true")) {
                holder.imgStatus.setImageResource(R.drawable.ic_check_green);
            } else {
                holder.imgStatus.setImageResource(R.drawable.ic_check_red);
            }
        }
        holder.imgNode.setImageResource(R.drawable.olmatixmed);
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

        holder.imgBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Nodes");
                builder.setMessage("What do you want to do?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.setTitle("Delete this Node?");
                        builder.setMessage(mInstalledNodeModel.getNice_name_n());

                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(position);
                                TSnackbar snackbar = TSnackbar.make((v), mInstalledNodeModel.getNice_name_n() + "Node deleted",TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#CC00CC"));
                                snackbar.show();
                                notifyDataSetChanged();

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                        builder.show();

                        dialog.cancel();

                    }
                });
                builder.setNegativeButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyDataSetChanged();
                        builder.setTitle("Rename Node");
                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);
                        if (mInstalledNodeModel.getNice_name_n()!=null) {
                            input.setText(mInstalledNodeModel.getNice_name_n());
                        } else{
                            input.setText(mInstalledNodeModel.getFwName());
                        }
                        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nice_name = input.getText().toString();
                                mInstalledNodeModel.setNodesID(mInstalledNodeModel.getNodesID());
                                mInstalledNodeModel.setNice_name_n(nice_name);
                                dbNodeRepo.updateNameNice(mInstalledNodeModel);
                                TSnackbar snackbar = TSnackbar.make((v), nice_name + " Renaming Node success",TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#CC00CC"));
                                snackbar.show();

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();

                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public static String calculateTime(long seconds) {

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

        String mNodeID = nodeList.get(position).getNodesID();
        for (int a=0; a < 12 ;a++) {
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
            if (a == 10) {topic = "devices/" + mNodeID + "/door/close";}
            if (a == 11) {topic = "devices/" + mNodeID + "/door/theft";}

            try {
                Connection.getClient().unsubscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Log.d("DEBUG", "removeItem: " +a +" / "+mNodeID);
        }

        InstalledNode.dbNodeRepo.deleteNode(nodeList.get(position).getNodesID());
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