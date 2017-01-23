package com.olmatix.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.ui.fragment.DashboardNode;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeDashboardAdapter extends RecyclerView.Adapter<NodeDashboardAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    List<DashboardNodeModel> nodeList;
    private Animation animConn;
    Context context;
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    public NodeDashboardAdapter(ArrayList<DashboardNodeModel> nodeList, Context dashboardnode, OnStartDragListener dragStartListener) {
        this.nodeList = nodeList;
        mDragStartListener = dragStartListener;
        this.context = dashboardnode;
    }

    @Override
    public int getItemViewType(int position) {

        int viewType = 0;
        if ((nodeList.get(position).getSensor().trim()).equals("light")) {
            viewType = 0;

        } else if ((nodeList.get(position).getSensor().trim()).equals("close")||(nodeList.get(position).getSensor().trim()).equals("motion")) {
            viewType = 1;
        }

        return viewType;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    public void removeItem(int position) {

        String mNodeID = nodeList.get(position).getId_node_detail();
        Log.d("DEBUG", "removeItem: "+mNodeID);
        DashboardNode.mDbNodeRepo.deleteFav(mNodeID);
        nodeList.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, nodeList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        animConn = AnimationUtils.loadAnimation(context, R.anim.blinkfast);

        switch (viewType) {
            case 0:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_button, viewGroup, false);

                return new ButtonHolder(v);

            case 1:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_status, viewGroup, false);

                return new StatusHolder(v);
            default:
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final DashboardNodeModel mFavoriteModel = nodeList.get(position);

        if ((mFavoriteModel.getSensor().trim()).equals("light")) {

            final ButtonHolder holder = (ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());

            if (mFavoriteModel.getStatus().trim().equals("false")) {
                holder.imgNode.setImageResource(R.drawable.offlamp1);
                holder.imgSending.setVisibility(View.GONE);

            } else {
                holder.imgNode.setImageResource(R.drawable.onlamp1);
                holder.imgSending.setVisibility(View.GONE);

            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
            } else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
            }

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1 = "ON";
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (mFavoriteModel.getOnline().trim().equals("true")) {
                            String topic = "devices/" + mFavoriteModel.getNodeid() + "/light/" + mFavoriteModel.getChannel() + "/set";
                            if (mFavoriteModel.getStatus().trim().equals("false")) {
                                payload1 = "ON";
                            } else {
                                payload1 = "OFF";
                            }
                            String payload = payload1;
                            byte[] encodedPayload = new byte[0];
                            try {
                                encodedPayload = payload.getBytes("UTF-8");
                                MqttMessage message = new MqttMessage(encodedPayload);
                                message.setQos(1);
                                message.setRetained(true);
                                Connection.getClient().publish(topic, message);
                                holder.imgSending.setVisibility(View.VISIBLE);
                                holder.imgSending.startAnimation(animConn);


                            } catch (UnsupportedEncodingException | MqttException e) {
                                e.printStackTrace();
                            }
                        } else{
                            TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d()+" Offline", TSnackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(Color.BLACK);
                            View snackbarView = snackbar.getView();
                                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                            Intent intent = new Intent("addNode");
                            intent.putExtra("Connect", "con");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });

        } else if ((mFavoriteModel.getSensor().trim()).equals("close")||(mFavoriteModel.getSensor().trim()).equals("motion")) {

            final StatusHolder holder = (StatusHolder) viewHolder;

            holder.node_names.setText(mFavoriteModel.getNice_name_d());

            if ((mFavoriteModel.getStatus().trim()).equals("true")) {
                holder.imgNodesBut.setImageResource(R.drawable.onsec);

            } else {
                holder.imgNodesBut.setImageResource(R.drawable.offsec);
            }

            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
            } else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
            }


            if ((mFavoriteModel.getStatus_sensor().trim().equals("true"))) {
                if ((mFavoriteModel.getSensor().trim()).equals("close")) {
                    holder.imgNodes.setImageResource(R.drawable.door_close);
                } else {
                    holder.imgNodes.setImageResource(R.drawable.motion);
                }
            } else {
                if ((mFavoriteModel.getSensor().trim()).equals("close")) {
                    holder.imgNodes.setImageResource(R.drawable.door_open);
                } else {
                    holder.imgNodes.setImageResource(R.drawable.no_motion);
                }

            }
            holder.imgNodesBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1;
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (mFavoriteModel.getOnline().trim().equals("true")) {

                            String topic = "devices/" + mFavoriteModel.getNodeid() + "/light/" + mFavoriteModel.getChannel() + "/set";
                            if (mFavoriteModel.getStatus().trim().equals("false")) {
                                payload1 = "ON";
                            } else {
                                payload1 = "OFF";
                            }
                            String payload = payload1;
                            byte[] encodedPayload = new byte[0];
                            try {
                                encodedPayload = payload.getBytes("UTF-8");
                                MqttMessage message = new MqttMessage(encodedPayload);
                                message.setQos(1);
                                message.setRetained(true);
                                Connection.getClient().publish(topic, message);
                            } catch (UnsupportedEncodingException | MqttException e) {
                                e.printStackTrace();
                            }
                        } else {
                            TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d()+" Offline", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Conn", "Conn1");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });
        } else if ((mFavoriteModel.getSensor().trim()).equals("close")) {

            final StatusHolder holder = (StatusHolder) viewHolder;

            holder.node_names.setText(mFavoriteModel.getNice_name_d());

            if ((mFavoriteModel.getStatus().trim()).equals("true")) {
                holder.imgNodesBut.setImageResource(R.drawable.onsec);

            } else {
                holder.imgNodesBut.setImageResource(R.drawable.offsec);
            }

            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
            } else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
            }


            if ((mFavoriteModel.getStatus_sensor().trim().equals("true"))) {
                holder.imgNodes.setImageResource(R.drawable.door_close);
            } else {
                holder.imgNodes.setImageResource(R.drawable.door_open);

            }
            holder.imgNodesBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1;
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (mFavoriteModel.getOnline().trim().equals("true")) {

                            String topic = "devices/" + mFavoriteModel.getNodeid() + "/light/" + mFavoriteModel.getChannel() + "/set";
                            if (mFavoriteModel.getStatus().trim().equals("false")) {
                                payload1 = "ON";
                            } else {
                                payload1 = "OFF";
                            }
                            String payload = payload1;
                            byte[] encodedPayload = new byte[0];
                            try {
                                encodedPayload = payload.getBytes("UTF-8");
                                MqttMessage message = new MqttMessage(encodedPayload);
                                message.setQos(1);
                                message.setRetained(true);
                                Connection.getClient().publish(topic, message);
                            } catch (UnsupportedEncodingException | MqttException e) {
                                e.printStackTrace();
                            }
                        } else {
                            TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d()+" Offline", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Conn", "Conn1");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class ButtonHolder extends ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline, imgSending;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            imgSending = (ImageView) view.findViewById(R.id.icon_sending);
        }
    }

    public class StatusHolder extends ViewHolder {
        public TextView node_names, status;
        public ImageView imgNodes, imgOnline;
        public ImageButton imgNodesBut;

        public StatusHolder(View view) {
            super(view);
            imgNodes = (ImageView) view.findViewById(R.id.icon_node);
            imgNodesBut = (ImageButton) view.findViewById(R.id.icon_node_button);
            node_names = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);

        }
    }

}