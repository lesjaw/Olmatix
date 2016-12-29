package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
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
    List<Dashboard_NodeModel> nodeList;

    public NodeDashboardAdapter(ArrayList<Dashboard_NodeModel> data, OnStartDragListener dragStartListener) {
        this.nodeList = data;
        mDragStartListener = dragStartListener;

    }

    @Override
    public int getItemViewType(int position) {

        int viewType = 0;
        if ((nodeList.get(position).getSensor().trim()).equals("light")) {
            viewType = 0;

        } else if ((nodeList.get(position).getSensor().trim()).equals("close")) {
            viewType = 1;
        }

        return viewType;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;

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

        final Dashboard_NodeModel mFavoriteModel = nodeList.get(position);

        if ((mFavoriteModel.getSensor().trim()).equals("light")) {

            final ButtonHolder holder = (ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());
            if (mFavoriteModel.getStatus().trim().equals("false")) {
                holder.imgNode.setImageResource(R.drawable.offlamp1);
            } else {
                holder.imgNode.setImageResource(R.drawable.onlamp1);
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
                    if (Connection.getClient().isConnected()) {
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
                    String payload1 = "ON";
                    if (Connection.getClient().isConnected()) {
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
        public ImageView imgOnline;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);

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