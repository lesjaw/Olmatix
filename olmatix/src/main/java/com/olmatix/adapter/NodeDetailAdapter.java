package com.olmatix.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by android on 12/13/2016.
 */

public class NodeDetailAdapter extends RecyclerView.Adapter<NodeDetailAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    List<DetailNodeModel> nodeList;
    Context context;
    String fw_name;
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    public NodeDetailAdapter(List<DetailNodeModel> nodeList, String fw_name, Context context, OnStartDragListener dragStartListener) {

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

        if (fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch")||fw_name.equals("smartadapter1ch")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_button, parent, false);

            return new OlmatixHolder(itemView);

        } else if (fw_name.equals("smartsensordoor")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor_door, parent, false);

            return new OlmatixSensorDoorHolder(itemView);
        } else if (fw_name.equals("smartsensormotion")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor_motion, parent, false);

            return new OlmatixSensorMotionHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final DetailNodeModel mInstalledNodeModel = nodeList.get(position);

        if (fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch")||fw_name.equals("smartadapter1ch")) {

            //Toast.makeText(context,"I m in",Toast.LENGTH_LONG).show();
            final OlmatixHolder holder = (OlmatixHolder) viewHolder;

            holder.fwName.setText(mInstalledNodeModel.getNode_id());
            holder.imgNode.setImageResource(R.drawable.olmatixmed);
            if(mInstalledNodeModel.getDuration()!=null) {
                holder.duration.setText("ON time : " + OlmatixUtils.getDuration(Long.valueOf(mInstalledNodeModel.getDuration())));
            }
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else
                holder.node_name.setText(mInstalledNodeModel.getName());


            //holder.upTime.setText("Uptime: "+OlmatixUtils.getScaledTime(Long.valueOf(mInstalledNodeModel.getUptime())));

            holder.status.setText(mInstalledNodeModel.getStatus());

            if (mInstalledNodeModel.getStatus().equals("true")) {
                holder.imgNode.setImageResource(R.mipmap.onlamp);
                holder.statuslabel.setText("Status:");
                holder.status.setText("ON");
                holder.status.setTextColor(Color.GREEN);
                //holder.status.setTextColor(ContextCompat.getColor(context, R.color.green));
                holder.btn_on.setEnabled(false);
                holder.btn_off.setEnabled(true);

            } else {
                holder.imgNode.setImageResource(R.mipmap.offlamp);
                holder.statuslabel.setText("Status:");
                holder.status.setText("OFF");
                holder.status.setTextColor(Color.RED);
                holder.btn_on.setEnabled(true);
                holder.btn_off.setEnabled(false);


            }
            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialog();
                }
            });

            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "ON";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" ON");
                            holder.status.setSingleLine();
                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context,"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                }
            });

            holder.btn_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "OFF";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" OFF");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context,"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                }

            });


        } else if (fw_name.equals("smartsensordoor")) {
            final OlmatixSensorDoorHolder holder = (OlmatixSensorDoorHolder) viewHolder;

            holder.imgNode.setImageResource(R.drawable.olmatixmed);
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else {
                holder.node_name.setText(mInstalledNodeModel.getName());
            }
            holder.fwName.setText(mInstalledNodeModel.getNode_id());
            holder.status.setText("Status : " + mInstalledNodeModel.getStatus());

            if (mInstalledNodeModel.getStatus_sensor().equals("true")) {
                holder.sensorStatus.setText("Door Close!");
                holder.imgSensor.setImageResource(R.drawable.door_close);
            } else {
                holder.sensorStatus.setText("Door Open!");
                holder.imgSensor.setImageResource(R.drawable.door_open);
            }
            if (mInstalledNodeModel.getStatus().equals("true")) {
                holder.imgNode.setImageResource(R.mipmap.armed);
                holder.statuslabel.setText("Status:");
                holder.status.setText("ARMED");


            } else {
                holder.imgNode.setImageResource(R.mipmap.not_armed);
                holder.statuslabel.setText("Status:");
                holder.status.setText("NOT ARMED");
            }
            if (mInstalledNodeModel.getStatus_theft().equals("true")) {
                holder.statuslabel.setText("Status:");
                holder.status.setText("ALARM!!");
                holder.status.setTextColor(Color.MAGENTA);
                holder.status.setTypeface(null, Typeface.BOLD);
            }
            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/0/set";
                        String payload = "ON";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(view, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                }
            });

            holder.btn_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    Log.d("DEBUG", "oNcLICK status connection: "+mStatusServer);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/0/set";
                        String payload = "OFF";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" NOT ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(view, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                }

            });
        } else if (fw_name.equals("smartsensormotion")) {
            final OlmatixSensorMotionHolder holder = (OlmatixSensorMotionHolder) viewHolder;

            holder.imgNode.setImageResource(R.drawable.olmatixmed);
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else {
                holder.node_name.setText(mInstalledNodeModel.getName());
            }
            holder.fwName.setText(mInstalledNodeModel.getNode_id());
            holder.status.setText("Status : " + mInstalledNodeModel.getStatus());

            if (mInstalledNodeModel.getStatus_sensor().equals("true")) {
                holder.sensorStatus.setText("Motion detected!");
                holder.imgSensor.setImageResource(R.drawable.motion);
            } else {
                holder.sensorStatus.setText("No Motion detected!");
                holder.imgSensor.setImageResource(R.drawable.no_motion);
            }
            if (mInstalledNodeModel.getStatus().equals("true")) {
                holder.imgNode.setImageResource(R.mipmap.armed);
                holder.statuslabel.setText("Status:");
                holder.status.setText("ARMED");


            } else {
                holder.imgNode.setImageResource(R.mipmap.not_armed);
                holder.statuslabel.setText("Status:");
                holder.status.setText("NOT ARMED");
            }
            if (mInstalledNodeModel.getStatus_theft().equals("true")) {
                holder.statuslabel.setText("Status:");
                holder.status.setText("ALARM!!");
                holder.status.setTextColor(Color.MAGENTA);
                holder.status.setTypeface(null, Typeface.BOLD);
            }
            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/0/set";
                        String payload = "ON";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(view, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                }
            });

            holder.btn_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    Log.d("DEBUG", "oNcLICK status connection: "+mStatusServer);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/0/set";
                        String payload = "OFF";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.statuslabel.setText("Sending");
                            holder.status.setText(" NOT ARMED");

                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(view, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
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

    public class OlmatixHolder extends ViewHolder {
        public TextView node_name, upTime, status, fwName, statuslabel, duration;
        public ImageView imgNode;
        Button btn_off, btn_on;

        public OlmatixHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            statuslabel = (TextView) view.findViewById(R.id.statuslabel);
            status = (TextView) view.findViewById(R.id.status);
            duration = (TextView) view.findViewById(R.id.duration);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);

        }
    }

    public class OlmatixSensorDoorHolder extends ViewHolder {
        public TextView node_name, upTime, status, sensorStatus, fwName, statuslabel;
        public ImageView imgNode, imgSensor;
        Button btn_off, btn_on;

        public OlmatixSensorDoorHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            sensorStatus = (TextView) view.findViewById(R.id.sensorstatus);
            status = (TextView) view.findViewById(R.id.status);
            statuslabel = (TextView) view.findViewById(R.id.statuslabel);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);
            imgSensor = (ImageView) view.findViewById(R.id.door);
        }
    }

    public class OlmatixSensorMotionHolder extends ViewHolder {
        public TextView node_name, upTime, status, sensorStatus, fwName, statuslabel;
        public ImageView imgNode, imgSensor;
        Button btn_off, btn_on;

        public OlmatixSensorMotionHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            sensorStatus = (TextView) view.findViewById(R.id.sensorstatus);
            status = (TextView) view.findViewById(R.id.status);
            statuslabel = (TextView) view.findViewById(R.id.statuslabel);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);
            imgSensor = (ImageView) view.findViewById(R.id.door);
        }
    }

    private void showAlertDialog() {
        // Prepare grid view
        final GridView gridView = new GridView(context);

        final ArrayList mList = new ArrayList<>();
        mList.add(R.drawable.onlamp1);
        mList.add(R.drawable.steckeron);

        final ArrayList<String> icon = new ArrayList();
        icon.add("R.drawable.onlamp1");
        icon.add("R.drawable.steckeroff");


        gridView.setAdapter(new iconPickerAdapter (context, R.layout.icon_picker, mList,icon));

        gridView.setNumColumns(4);
        gridView.setHorizontalSpacing(0);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something here
                view.setSelected(true);
                Log.d("DEBUG", "onClick1: "+gridView.getSelectedItem().toString());

            }
        });

        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(gridView);
        builder.setTitle("Pick Icon");
        builder.show();
    }

}

