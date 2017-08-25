package com.olmatix.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.logModel;
import com.olmatix.ui.activity.ChartONOFF;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by android on 12/13/2016.
 */

public class NodeDetailAdapter extends RecyclerView.Adapter<NodeDetailAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private List<DetailNodeModel> nodeList;
    Context context;
    private String fw_name;
    private SharedPreferences sharedPref;
    private Boolean mStatusServer;
    private int step = 50;
    private int min = 50;
    private int position1;
    private int UNSELECTED = -1;
    private int selectedItem = UNSELECTED;
    private int tog =1;
    String cur_red="0";
    String cur_green="0";
    String cur_blue="0";
    String cur_white="0";
    private dbNodeRepo mDbNodeRepo;
    private ArrayList<logModel> datalog;

    public NodeDetailAdapter(List<DetailNodeModel> nodeList, String fw_name, Context context, OnStartDragListener dragStartListener) {

        this.nodeList = nodeList;
        this.fw_name = fw_name;
        //mDragStartListener = dragStartListener;
        this.context = context;

    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if (fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch")
                ||fw_name.equals("smartadapter1ch")||fw_name.equals("smartadapter8ch")) {
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
        } else if (fw_name.equals("smartsensortemp")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor_temp, parent, false);

            return new OlmatixHolder(itemView);
        } else if (fw_name.equals("smartsensorprox")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor_prox, parent, false);

            return new OlmatixSensorProxHolder(itemView);
        } else if (fw_name.equals("smartrgb")||fw_name.equals("smartrgbw")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_rgb, parent, false);

            return new OlmatixRGBHolder(itemView);
        }

        return null;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final DetailNodeModel mInstalledNodeModel = nodeList.get(position);
        this.position1 = position;

        if (fw_name.equals("smartfitting") || fw_name.equals("smartadapter4ch")
                ||fw_name.equals("smartadapter1ch")||fw_name.equals("smartadapter8ch")) {

            final OlmatixHolder holder = (OlmatixHolder) viewHolder;

            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    //Log.d("DEBUG", "onBindViewHolder ON: "+on);
                    String off = datalog.get(i).getOff();
                    //Log.d("DEBUG", "onBindViewHolder OFF: "+on);

                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+"\nChannel : "+ch);
            holder.imgNode.setImageResource(R.drawable.olmatixmed);

            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();

            holder.api2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+"&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";

            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            if(mInstalledNodeModel.getDuration()!=null) {
                holder.duration.setText("ON time : " + OlmatixUtils.getDuration(Long.valueOf(mInstalledNodeModel.getDuration())));
            }
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else
                holder.node_name.setText(mInstalledNodeModel.getName());

            holder.status.setText(mInstalledNodeModel.getStatus());

            String lastval=mInstalledNodeModel.getStatus();

            if (lastval!=null && !lastval.equals("")) {
                if (mInstalledNodeModel.getStatus().equals("on")) {
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "on";
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "off";
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

            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }

                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
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

            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    String off = datalog.get(i).getOff();
                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+"\nChannel : "+ch);
            holder.status.setText("Status : " + mInstalledNodeModel.getStatus());

            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();
            holder.api2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";
            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            if (mInstalledNodeModel.getStatus_sensor().equals("true")) {
                holder.sensorStatus.setText("Door Close!");
                holder.imgSensor.setImageResource(R.drawable.door_close);
            } else {
                holder.sensorStatus.setText("Door Open!");
                holder.imgSensor.setImageResource(R.drawable.door_open);
            }
            if (mInstalledNodeModel.getStatus().equals("on")) {
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
                holder.imgNode.setImageResource(R.drawable.theft);
            }
            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "on";
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "off";
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

            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }

                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
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
            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    String off = datalog.get(i).getOff();
                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+"\nChannel : "+ch);
            holder.status.setText("Status : " + mInstalledNodeModel.getStatus());

            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();
            holder.api2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";
            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            if (mInstalledNodeModel.getStatus_sensor().equals("true")) {
                holder.sensorStatus.setText("Motion detected!");
                holder.imgSensor.setImageResource(R.drawable.motion);
            } else {
                holder.sensorStatus.setText("No Motion detected!");
                holder.imgSensor.setImageResource(R.drawable.no_motion);
            }
            if (mInstalledNodeModel.getStatus().equals("on")) {
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
                holder.imgNode.setImageResource(R.drawable.theft);

            }
            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "on";
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "off";
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

            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }

                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
                }
            });

        } else if (fw_name.equals("smartsensortemp")) {
            final OlmatixHolder holder = (OlmatixHolder) viewHolder;

            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    String off = datalog.get(i).getOff();
                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+"\nChannel : "+ch);
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

            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();
            holder.api2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";
            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            if (mInstalledNodeModel.getStatus().equals("on")) {
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
            String t=mInstalledNodeModel.getStatus_temp();
            Log.d("DEBUG", "tempe: "+t);
            if (t !=null) {
                if (!t.equals("0")) {
                    if (!t.equals("nan")) {
                        String t1 = t.substring(0, 2);
                        int t2 = Integer.parseInt(t1.replaceAll("[\\D]", ""));
                        holder.temp.setText(t2 - 7 + "°C");
                        String h = mInstalledNodeModel.getStatus_hum();
                        String h1 = h.substring(0, 2);
                        int h2 = Integer.parseInt(h1.replaceAll("[\\D]", ""));
                        holder.hum.setText(h2 + "%");
                    }
                }
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "on";
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
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_" + mInstalledNodeModel.getChannel() + "/set";
                        String payload = "off";
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
            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }

                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
                }
            });

        } else if (fw_name.equals("smartsensorprox")) {
            final OlmatixSensorProxHolder holder = (OlmatixSensorProxHolder) viewHolder;

            holder.imgNode.setImageResource(R.drawable.olmatixmed);
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else {
                holder.node_name.setText(mInstalledNodeModel.getName());
            }
            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    String off = datalog.get(i).getOff();
                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+"\nChannel : "+ch);
            holder.status.setText("Status : " + mInstalledNodeModel.getStatus());

            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();
            holder.api2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";
            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            if (mInstalledNodeModel.getStatus_sensor().equals("true")) {
                holder.sensorStatus.setText("Block detected!");
                holder.imgSensor.setImageResource(R.drawable.proximityon);
            } else {
                holder.sensorStatus.setText("Empty");
                holder.imgSensor.setImageResource(R.drawable.proximityoff);
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
                holder.imgNode.setImageResource(R.drawable.theft);

            }

            if (mInstalledNodeModel.getStatus_jarak()!=null) {
                holder.jarak.setText(mInstalledNodeModel.getStatus_jarak());
                float convert = Integer.parseInt(mInstalledNodeModel.getStatus_jarak());
                String jar;
                if (convert < 100) {
                    jar = convert + " cm";
                } else {
                    convert = convert / 100;
                    jar = convert + " m";
                }
                holder.jarak.setText(jar);
            }

            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "on";
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
                    Log.d("DEBUG", "oNcLICK status connection: " + mStatusServer);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/light/on_1/set";
                        String payload = "off";
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
            holder.setrange.setText(mInstalledNodeModel.getStatus_range()+ " cm");
            int max = 200;
            holder.seekRange.setMax( (max - min) / step );
           /* int steppos = Integer.parseInt(mInstalledNodeModel.getStatus_range());

            if (steppos == 200) {
                holder.seekRange.setProgress(4);
            } else if (steppos == 150) {
                holder.seekRange.setProgress(3);
            } else if (steppos == 100) {
                holder.seekRange.setProgress(2);
            }else if (steppos == 50) {
                holder.seekRange.setProgress(1);
            }*/

            holder.seekRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = min + (progress * step);
                    //Log.d("DEBUG", "onProgressChanged: "+value);
                    String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/dist/range/set";
                    String payload = String.valueOf(value);
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
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }

                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
                }
            });
        } else if (fw_name.equals("smartrgb")||fw_name.equals("smartrgbw")) {
            final OlmatixRGBHolder holder = (OlmatixRGBHolder) viewHolder;

            holder.colorPickerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    holder.colclick=0;
                    return false;
                }
            });

            Log.d("DEBUG", "Current Tog: "+tog);

            if (tog==0){
                holder.redSl.setVisibility(View.VISIBLE);
                holder.greenSl.setVisibility(View.VISIBLE);
                holder.blueSl.setVisibility(View.VISIBLE);
                holder.colorPickerView.setVisibility(View.GONE);
                holder.v_lightness_slider.setVisibility(View.GONE);
            } else {
                holder.redSl.setVisibility(View.GONE);
                holder.greenSl.setVisibility(View.GONE);
                holder.blueSl.setVisibility(View.GONE);
                holder.colorPickerView.setVisibility(View.VISIBLE);
                holder.v_lightness_slider.setVisibility(View.VISIBLE);
            }
            holder.imgNode.setImageResource(R.mipmap.smartrgb);
            holder.mode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("DEBUG", "Current Tog: "+tog);
                    if (tog == 1) {
                        holder.redSl.setVisibility(View.VISIBLE);
                        holder.greenSl.setVisibility(View.VISIBLE);
                        holder.blueSl.setVisibility(View.VISIBLE);
                        holder.colorPickerView.setVisibility(View.GONE);
                        holder.v_lightness_slider.setVisibility(View.GONE);

                        tog = 0;
                    } else {
                        holder.redSl.setVisibility(View.GONE);
                        holder.greenSl.setVisibility(View.GONE);
                        holder.blueSl.setVisibility(View.GONE);
                        holder.colorPickerView.setVisibility(View.VISIBLE);
                        holder.v_lightness_slider.setVisibility(View.VISIBLE);

                        tog = 1;
                    }
                }
            });

            holder.redSl.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            holder.redSl.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

            holder.greenSl.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            holder.greenSl.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

            holder.blueSl.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
            holder.blueSl.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

            holder.redSl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = (progress);
                    Log.d("DEBUG", "onProgressChanged: "+value);
                    String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                    String payload = "rgb("+String.valueOf(value)+","+cur_green+","+cur_blue+","+cur_white+")";
                    cur_red = String.valueOf(value);
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
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.greenSl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value =(progress);
                    Log.d("DEBUG", "onProgressChanged: "+value);
                    String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                    String payload = "rgb("+cur_red+","+String.valueOf(value)+","+cur_blue+","+cur_white+")";
                    cur_green = String.valueOf(value);
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
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.blueSl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = (progress);
                    Log.d("DEBUG", "onProgressChanged: "+value);
                    String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                    String payload = "rgb("+cur_red+","+cur_green+","+String.valueOf(value)+","+cur_white+")";
                    cur_blue = String.valueOf(value);
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
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.whiteSl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = (progress);
                    Log.d("DEBUG", "onProgressChanged: "+value);
                    String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                    String payload = "rgb("+cur_red+","+cur_green+","+cur_blue+","+String.valueOf(value)+")";
                    cur_white = String.valueOf(value);
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
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
                @Override public void onColorChanged(int selectedColor) {
                    // Handle on color change
                    //holder.colclick=0;

                    if (holder.colclick==0) {
                        holder.colclick=1;
                        int red = Color.red(selectedColor);
                        int green = Color.green(selectedColor);
                        int blue = Color.blue(selectedColor);
                        //Log.d("ColorPicker", "onColorChanged: red" + red + " green " + green + " blue " + blue);
                        holder.status.setText("RGB(" + red + "," + green + "," + blue + ")");

                        //holder.imgNode.setImageTintList(ColorStateList.valueOf(selectedColor));
                        //Log.d("DEBUG", "onColorChanged: "+selectedColor);
                        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                        mStatusServer = sharedPref.getBoolean("conStatus", false);
                        if (mStatusServer) {
                            String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                            String payload = "rgb(" + red + "," + green + "," + blue+ ",0" + ")";
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
                        /*TSnackbar snackbar = TSnackbar.make(view, "You dont connect to server", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();*/
                            Intent intent = new Intent("addNode");
                            intent.putExtra("Connect", "con");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                }
            });
            holder.colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
                @Override
                public void onColorSelected(int selectedColor) {

                }
            });

            //holder.imgNode.setImageResource(R.drawable.olmatixmed);
            if (mInstalledNodeModel.getNice_name_d() != null) {
                holder.node_name.setText(mInstalledNodeModel.getNice_name_d());
            } else {
                holder.node_name.setText(mInstalledNodeModel.getName());
            }
            String ch = mInstalledNodeModel.getChannel();
            String dateString = null;
            datalog.clear();
            datalog.addAll(mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()));
            int countDB = mDbNodeRepo.getLogbyName(mInstalledNodeModel.getNode_id(),mInstalledNodeModel.getChannel()).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String Nodeid = datalog.get(i).getNodeid();
                    String chan = datalog.get(i).getChannel();
                    String on = datalog.get(i).getOn();
                    String off = datalog.get(i).getOff();
                    String timestamps;
                    if (off.equals("0")||off == null){
                        timestamps = on;
                    } else  {
                        timestamps = off;
                    }

                    //String time = timestamps.getText().toString();

                    long timestampsformat = Long.parseLong(timestamps);;

                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                    dateString = timeformat.format(new Date(timestampsformat));
                }
            }

            holder.lastaction.setText("Last log : "+dateString);
            holder.fwName.setText(mInstalledNodeModel.getNode_id()+" Channel : "+ch);
            String lastval=mInstalledNodeModel.getStatus();
            //Log.d("DEBUG", "RGB: " + lastval+ " nodeid "+mInstalledNodeModel.getNode_id()+" Channel : "+ch);

            holder.status.setText(mInstalledNodeModel.getStatus());
            String convRGB = mInstalledNodeModel.getStatus();
            Log.d("DEBUG", "onBindViewHolder RGB: "+convRGB.length());
            int colorVal = 0;
            if (convRGB.length()>10) {
                String[] a = convRGB.split(",");
                int redval = Integer.parseInt(a[0].substring(4));
                int greenVal = Integer.parseInt(a[1]);
                int blueVal = Integer.parseInt(a[2]);

                //int blueVal = Integer.parseInt(a[2].substring(0, a[2].length()-1));
                colorVal = Color.rgb(redval, greenVal, blueVal);
            }
            //Log.d("DEBUG", "onBindViewHolder: "+colorVal);
            holder.imgNode.setColorFilter(colorVal, PorterDuff.Mode.SRC_IN);
            holder.api2.setText(Html.fromHtml("<font color='#ffffff'>"+"API for <b>getting ON/OFF</b> status this Node </font>"));
            String apiget2 = "http://cloud.olmatix.com:1880/API/GET/SWITCH?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel();
            holder.api2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget2);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget2);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.api1.setText(Html.fromHtml("<font color='#ffffff'>"+"API for sending <b>command ON/OFF</b> to this Node </font>"));
            String apiget1 = "http://cloud.olmatix.com:1880/API/POST?id="+mInstalledNodeModel.getNode_id()+
                    "&ch="+mInstalledNodeModel.getChannel()+"&msg=ON";
            holder.api1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", apiget1);
                    clipboard.setPrimaryClip(clip);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, apiget1);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            });

            holder.btn_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                        String payload = "rgb(255,255,255,255)";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText(" ON");

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
                    Log.d("DEBUG", "oNcLICK status connection: " + mStatusServer);
                    if (mStatusServer) {
                        String topic = "devices/" + mInstalledNodeModel.getNode_id() + "/led/color/set";
                        String payload = "rgb(0,0,0,0)";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                            holder.status.setText(" OFF");
                            holder.colclick=0;

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

            holder.imgBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (holder != null) {
                        holder.imgBut.setSelected(false);
                        holder.expandableLayout.collapse();
                    }
                    if (position1 == selectedItem) {
                        selectedItem = UNSELECTED;
                    } else {
                        holder.imgBut.setSelected(true);
                        holder.expandableLayout.expand();
                        selectedItem = position1;
                    }
                }
            });

            holder.seechart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ChartONOFF.class);
                    //i.putExtra("nodeid", data.get(position).getNodesID());
                    i.putExtra("nice_name", mInstalledNodeModel.getNice_name_d());
                    context.startActivity(i);
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
        public TextView node_name, upTime, status, fwName, statuslabel, duration, temp, hum,
                lastaction, seechart, api1, api2;
        public ImageView imgNode;
        Button btn_off, btn_on;
        ImageButton imgBut;
        ExpandableLayout expandableLayout;

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
            temp = (TextView) view.findViewById(R.id.temp);
            hum = (TextView) view.findViewById(R.id.hum);
            imgBut = (ImageButton) view.findViewById(R.id.opt);
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            lastaction = (TextView) view.findViewById(R.id.lastdata);
            seechart = (TextView) view.findViewById(R.id.chartLabel);
            api1 = (TextView) view.findViewById(R.id.API1);
            api2 = (TextView) view.findViewById(R.id.API2);

            datalog = new ArrayList<>();
            mDbNodeRepo = new dbNodeRepo(context);

        }

    }

    public class OlmatixSensorDoorHolder extends ViewHolder {
        public TextView node_name, upTime, status, sensorStatus, fwName, statuslabel, lastaction, seechart, api1, api2;
        public ImageView imgNode, imgSensor;
        Button btn_off, btn_on;
        ExpandableLayout expandableLayout;
        ImageButton imgBut;

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
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            lastaction = (TextView) view.findViewById(R.id.lastdata);
            seechart = (TextView) view.findViewById(R.id.chartLabel);
            imgBut = (ImageButton) view.findViewById(R.id.opt);
            api1 = (TextView) view.findViewById(R.id.API1);
            api2 = (TextView) view.findViewById(R.id.API2);
            datalog = new ArrayList<>();
            mDbNodeRepo = new dbNodeRepo(context);
        }
    }

    public class OlmatixSensorMotionHolder extends ViewHolder {
        public TextView node_name, upTime, status, sensorStatus, fwName, statuslabel, lastaction, seechart, api1, api2;
        public ImageView imgNode, imgSensor;
        Button btn_off, btn_on;
        ExpandableLayout expandableLayout;
        ImageButton imgBut;

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
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            lastaction = (TextView) view.findViewById(R.id.lastdata);
            seechart = (TextView) view.findViewById(R.id.chartLabel);
            imgBut = (ImageButton) view.findViewById(R.id.opt);
            api1 = (TextView) view.findViewById(R.id.API1);
            api2 = (TextView) view.findViewById(R.id.API2);
            datalog = new ArrayList<>();
            mDbNodeRepo = new dbNodeRepo(context);

        }
    }

    public class OlmatixSensorProxHolder extends ViewHolder {
        public TextView node_name, upTime, status, sensorStatus, fwName, statuslabel, jarak, setrange, lastaction, seechart, api1, api2;
        public ImageView imgNode, imgSensor;
        public SeekBar seekRange;
        Button btn_off, btn_on;
        ExpandableLayout expandableLayout;
        ImageButton imgBut;


        public OlmatixSensorProxHolder(View view) {
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
            jarak =(TextView) view.findViewById(R.id.jarak);
            seekRange = (SeekBar)view.findViewById(R.id.seek_range);
            setrange = (TextView) view.findViewById(R.id.set_range_text);
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            lastaction = (TextView) view.findViewById(R.id.lastdata);
            seechart = (TextView) view.findViewById(R.id.chartLabel);
            imgBut = (ImageButton) view.findViewById(R.id.opt);
            api1 = (TextView) view.findViewById(R.id.API1);
            api2 = (TextView) view.findViewById(R.id.API2);
            datalog = new ArrayList<>();
            mDbNodeRepo = new dbNodeRepo(context);
        }
    }

    public class OlmatixRGBHolder extends ViewHolder {
        public TextView node_name, upTime, status, fwName, lastaction, seechart, api1, api2;
        public ImageView imgNode;
        Button btn_off, btn_on;
        ExpandableLayout expandableLayout;
        ImageButton imgBut, mode;
        ColorPickerView colorPickerView;
        int colclick;
        SeekBar redSl, greenSl, blueSl, whiteSl;
        LightnessSlider v_lightness_slider;

        public OlmatixRGBHolder(View view) {
            super(view);
            colorPickerView = (ColorPickerView) view.findViewById(R.id.color_picker_view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            fwName = (TextView) view.findViewById(R.id.fw_name);
            status = (TextView) view.findViewById(R.id.status);
            upTime = (TextView) view.findViewById(R.id.uptime);
            btn_off = (Button) view.findViewById(R.id.btn_off);
            btn_on = (Button) view.findViewById(R.id.btn_on);
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            lastaction = (TextView) view.findViewById(R.id.lastdata);
            seechart = (TextView) view.findViewById(R.id.chartLabel);
            imgBut = (ImageButton) view.findViewById(R.id.opt);
            mode = (ImageButton) view.findViewById(R.id.mode);
            api1 = (TextView) view.findViewById(R.id.API1);
            api2 = (TextView) view.findViewById(R.id.API2);
            redSl = (SeekBar) view.findViewById(R.id.red);
            greenSl = (SeekBar) view.findViewById(R.id.green);
            blueSl = (SeekBar) view.findViewById(R.id.blue);
            whiteSl = (SeekBar) view.findViewById(R.id.white);

            v_lightness_slider = (LightnessSlider) view.findViewById(R.id.v_lightness_slider);

            datalog = new ArrayList<>();
            mDbNodeRepo = new dbNodeRepo(context);
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

