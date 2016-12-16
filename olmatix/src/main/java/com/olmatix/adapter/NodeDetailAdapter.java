package com.olmatix.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Detail_NodeModel;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * Created by android on 12/13/2016.
 */

public class NodeDetailAdapter  extends RecyclerView.Adapter<NodeDetailAdapter.ViewHolder> implements ItemTouchHelperAdapter
{

    List<Detail_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;
    String node_name;
    Context context;
    String fw_name;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class OlmatixHolder extends ViewHolder {
        public TextView node_name, upTime, status, fwName;
        public ImageView imgNode;
        Button btn_off, btn_on;

        public OlmatixHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            status = (TextView) view.findViewById(R.id.status);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);

        }
    }

    public class OlmatixSensorHolder extends ViewHolder {
        public TextView node_name, upTime, status,sensorStatus, fwName;
        public ImageView imgNode, imgSensor;
        Button btn_off, btn_on;

        public OlmatixSensorHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            sensorStatus = (TextView) view.findViewById(R.id.sensorstatus);
            status = (TextView) view.findViewById(R.id.status);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);
            imgSensor = (ImageView) view.findViewById(R.id.door);

        }
    }

    public NodeDetailAdapter(List<Detail_NodeModel> nodeList,String fw_name,Context context,OnStartDragListener dragStartListener) {

        this.nodeList = nodeList;
        this.fw_name = fw_name;
        mDragStartListener = dragStartListener;
        this.context = context;


    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if (fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_button, parent, false);

              return new OlmatixHolder(itemView);

        }
        else if(fw_name.equals("smartsensordoor"))
        {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor, parent, false);

            return new OlmatixSensorHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //final int pos = position;
        final Detail_NodeModel mInstalledNodeModel = nodeList.get(position);
        if(fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch"))
        {

            //Toast.makeText(context,"I m in",Toast.LENGTH_LONG).show();
            final OlmatixHolder holder = (OlmatixHolder) viewHolder;
            holder.fwName.setText(mInstalledNodeModel.getNode_id());
            holder.imgNode.setImageResource(R.drawable.olmatixlogo);

            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            }
             else holder.node_name.setText(mInstalledNodeModel.getName());



            holder.upTime.setText(mInstalledNodeModel.getUptime());

            holder.status.setText("Status : "+mInstalledNodeModel.getStatus());

            if (mInstalledNodeModel.getStatus().equals("true")){
                holder.imgNode.setImageResource(R.mipmap.onlamp);
                holder.status.setText("Status : "+"ON");

            }else {
                holder.imgNode.setImageResource(R.mipmap.offlamp);
                holder.status.setText("Status : " + "OFF");
            }
            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Connection.getClient().isConnected()) {
                        String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/"+mInstalledNodeModel.getChannel()+"/set";
                        String payload = "ON";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText("ON");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else
                    {}

                }
            });
            //Log.e("status",mInstalledNodeModel.getStatus());

            holder.btn_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Connection.getClient().isConnected()) {
                        String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/"+mInstalledNodeModel.getChannel()+"/set";
                        String payload = "OFF";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText("OFF");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else
                    {}

                }

            });


        }else if(fw_name.equals("smartsensordoor"))
        {
            final OlmatixSensorHolder holder = (OlmatixSensorHolder) viewHolder;

            holder.imgNode.setImageResource(R.drawable.olmatixlogo);
            if (mInstalledNodeModel.getName() != null) {
                holder.node_name.setText(mInstalledNodeModel.getName());
            }

            else    if (mInstalledNodeModel.getNice_name_d()!= null){
                    holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
                }

            holder.fwName.setText(mInstalledNodeModel.getNode_id());

            holder.upTime.setText(mInstalledNodeModel.getUptime());
            holder.status.setText("Status : "+mInstalledNodeModel.getStatus());

                if(mInstalledNodeModel.getStatus_sensor().equals("true")) {
                    holder.sensorStatus.setText("Door Close!");
                    holder.imgSensor.setImageResource(R.drawable.door_close);
                }else {
                    holder.sensorStatus.setText("Door Open!");
                    holder.imgSensor.setImageResource(R.drawable.door_open);
                }


            if (mInstalledNodeModel.getStatus().equals("true")){
                holder.imgNode.setImageResource(R.mipmap.armed);
                holder.status.setText("Status : "+"ARMED");

            }else {
                holder.imgNode.setImageResource(R.mipmap.not_armed);
                holder.status.setText("Status : " + "NOT ARMED");
                //if (mInstalledNodeModel.getStatus_theft().equals("true")){
                  //  holder.status.setText("Status : " + "ALARM!!");
                    //holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                //}
            }
            Log.d("DEBUG", "Adapter: " +mInstalledNodeModel.getStatus_theft());


            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Connection.getClient().isConnected()) {
                        String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/0/set";
                        String payload = "ON";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText("ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else
                    {}

                }
            });

            holder.btn_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Connection.getClient().isConnected()) {
                        String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/0/set";
                        String payload = "OFF";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText("NOT ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else
                    {}

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


}

