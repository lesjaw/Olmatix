package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Detail_NodeModel;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by android on 12/13/2016.
 */

public class NodeDetailAdapter  extends RecyclerView.Adapter<NodeDetailAdapter.OlmatixHolder>
{

    List<Detail_NodeModel> nodeList;


    public class OlmatixHolder extends RecyclerView.ViewHolder {
        public TextView node_name, upTime, status;
        public ImageView imgNode;
        Button btn_off, btn_on;

        public OlmatixHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            status = (TextView) view.findViewById(R.id.status);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);

        }
    }

    public NodeDetailAdapter(List<Detail_NodeModel> nodeList) {

        this.nodeList = nodeList;

    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public OlmatixHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_node_button, parent, false);

        return new OlmatixHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OlmatixHolder holder, final int position) {
        final int pos = position;
        Log.d("pos", "onBindViewHolder: "+pos);

        final Detail_NodeModel mInstalledNodeModel = nodeList.get(position);

        holder.imgNode.setImageResource(R.drawable.olmatixlogo);
        if (mInstalledNodeModel.getName() != null) {

            holder.node_name.setText(mInstalledNodeModel.getName());
        }
        holder.upTime.setText(mInstalledNodeModel.getUptime());
        holder.status.setText(mInstalledNodeModel.getStatus());

        holder.btn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.getClient().isConnected()) {
                    String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/"+String.valueOf(pos)+"/set";
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

        holder.btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.getClient().isConnected()) {
                    String topic = "devices/"+mInstalledNodeModel.getNode_id()+"/light/"+String.valueOf(pos)+"/set";
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


    }

}