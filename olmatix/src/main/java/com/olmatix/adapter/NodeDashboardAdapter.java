package com.olmatix.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.CubeGrid;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.olmatix.database.dbNode;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.UdpClientThread;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.DurationModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.activity.MainActivity;
import com.olmatix.ui.fragment.DashboardNode;
import com.olmatix.utils.Connection;

import org.appspot.olmatixrtc.ConnectActivity;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeDashboardAdapter extends RecyclerView.Adapter<NodeDashboardAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    //private final OnStartDragListener mDragStartListener;
    private List<DashboardNodeModel> nodeList;
    public Context context;
    private SharedPreferences sharedPref;
    private Boolean mStatusServer;
    private dbNodeRepo mDbNodeRepo;
    private ArrayList<InstalledNodeModel> nodesInstalled;
    ArrayList<DetailNodeModel> data1;
    ArrayList<DurationModel> data3;
    private String ip_nodes = null, wifi_name;
    private UdpClientHandler udpClientHandler;
    private static UdpClientThread udpClientThread;
    private String node_id, Channel;
    private DurationModel durationModel;
    private dbNode dbnode;
    private DetailNodeModel detailNodeModel;

    public NodeDashboardAdapter(ArrayList<DashboardNodeModel> nodeList, Context dashboardnode, DashboardNode dashboardNode) {
        this.nodeList = nodeList;
        this.context = dashboardnode;

    }

    @Override
    public int getItemViewType(int position) {

        int viewType = 0;
        if ((nodeList.get(position).getSensor().trim()).equals("light")) {
            viewType = 0;

        } else if ((nodeList.get(position).getSensor().trim()).equals("close")
                ||(nodeList.get(position).getSensor().trim()).equals("motion")
                ||(nodeList.get(position).getSensor().trim()).equals("prox")) {
            viewType = 1;

        } else if ((nodeList.get(position).getSensor().trim()).equals("temp")) {
            viewType = 2;
        } else if ((nodeList.get(position).getSensor().trim()).equals("olmatixapp")) {
            viewType = 3;
        } else if ((nodeList.get(position).getSensor().trim()).equals("rgb")) {
            viewType = 4;
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
        DashboardNode.mDbNodeRepo.deleteFav(String.valueOf(mNodeID));
        nodeList.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, nodeList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        nodesInstalled = new ArrayList<>();
        data1 = new ArrayList<>();
        data3 = new ArrayList<>();
        mDbNodeRepo = new dbNodeRepo(context);
        udpClientHandler = new UdpClientHandler(this);
        durationModel = new DurationModel();
        dbnode = new dbNode();
        detailNodeModel = new DetailNodeModel();

        switch (viewType) {
            case 0:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_button, viewGroup, false);

                return new ButtonHolder(v);

            case 1:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_status, viewGroup, false);

                return new StatusHolder(v);
            case 2:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_temp, viewGroup, false);

                return new TempHolder(v);
            case 3:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_phone, viewGroup, false);

                return new PhoneHolder(v);
            case 4:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_rgb, viewGroup, false);

                return new RGBHolder(v);
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

            String lastval=mFavoriteModel.getStatus();

            if (lastval!=null && !lastval.equals("")) {
                if (mFavoriteModel.getStatus().trim().equals("off")) {
                    holder.imgNode.setImageResource(R.drawable.offlamp1);
                } else {
                    holder.imgNode.setImageResource(R.drawable.onlamp1);
                }
                holder.loading.setVisibility(View.GONE);
            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.on_indicator);

            } else {
                holder.imgOnline.setImageResource(R.drawable.off_indicator);
            }

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1 = "on";
                    if (mFavoriteModel.getStatus().trim().equals("off")) {
                        payload1 = "on";
                    } else {
                        payload1 = "off";
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String ip_devices = sharedPref.getString("IPaddress", "127.0.0.1");
                    String ssid_devices = sharedPref.getString("WiFi_SSID", "None");

                    ip_nodes = "";
                    wifi_name = "";

                    nodesInstalled.clear();
                    nodesInstalled.addAll(mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()));
                    int countDB = mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()).size();
                    if (countDB != 0) {
                        for (int i = 0; i < countDB; i++) {
                            ip_nodes = nodesInstalled.get(i).getLocalip();
                            wifi_name = nodesInstalled.get(i).getIcon();
                        }
                    }

                    String ipDev = ip_devices.substring(0, 10);
                    String ipNod = ip_nodes.substring(0, 10);
                    Log.d("DEBUG", "WiFi Compare : " + ssid_devices + " : " + wifi_name);

                    if (!mFavoriteModel.getOnline().trim().equals("true")) {
                        TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d() + " Offline, we check this node now" +
                                " by sending a request", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();

                        new getOnline(mFavoriteModel.getNodeid(), v).execute();
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            executeCommandviaInet(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            holder.loading.setVisibility(View.VISIBLE);
                            holder.loading.setScaleY(4f);
                        }

                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server, executing command via local WiFi", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            Toast.makeText(context, "The Node you are trying to execute is not avalaible in current network " +
                                    "Device ID: " + mFavoriteModel.getNodeid() + " Network: " + wifi_name, Toast.LENGTH_SHORT).show();

                        }
                    }

                }
            });

            holder.imgNode.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(mFavoriteModel.getNice_name_d());
                    String nice_name = mFavoriteModel.getNice_name_d();

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return false;
                }
            });

        } else if ((mFavoriteModel.getSensor().trim()).equals("close")||(mFavoriteModel.getSensor().trim()).equals("motion")||(mFavoriteModel.getSensor().trim()).equals("prox")) {

            final StatusHolder holder = (StatusHolder) viewHolder;

            holder.node_names.setText(mFavoriteModel.getNice_name_d());

            if ((mFavoriteModel.getStatus().trim()).equals("on")) {
                holder.imgNodesBut.setImageResource(R.drawable.onsec);
                holder.loading.setVisibility(View.GONE);


            } else {
                holder.imgNodesBut.setImageResource(R.drawable.offsec);
                holder.loading.setVisibility(View.GONE);

            }

            String theft = mFavoriteModel.getStatus_theft();
            if (theft.equals("true")){
                holder.imgNodesBut.setImageResource(R.drawable.theft);
            }

            String lastval=mFavoriteModel.getOnline();
            if (lastval!=null && !lastval.equals("")) {
                if (mFavoriteModel.getOnline().trim().equals("true")) {
                    holder.imgOnline.setImageResource(R.drawable.on_indicator);
                } else {
                    holder.imgOnline.setImageResource(R.drawable.off_indicator);
                }
            }

            if ((mFavoriteModel.getStatus_sensor().trim().equals("true"))) {
                if ((mFavoriteModel.getSensor().trim()).equals("close")) {
                    holder.imgNodes.setImageResource(R.drawable.door_close);
                } else if ((mFavoriteModel.getSensor().trim()).equals("motion")){
                    holder.imgNodes.setImageResource(R.drawable.motion);
                } else if ((mFavoriteModel.getSensor().trim()).equals("prox")){
                    holder.imgNodes.setImageResource(R.drawable.proximityon);
                }
            } else {
                if ((mFavoriteModel.getSensor().trim()).equals("close")) {
                    holder.imgNodes.setImageResource(R.drawable.door_open);
                } else if ((mFavoriteModel.getSensor().trim()).equals("motion")) {
                    holder.imgNodes.setImageResource(R.drawable.no_motion);
                }else if ((mFavoriteModel.getSensor().trim()).equals("prox")) {
                    holder.imgNodes.setImageResource(R.drawable.proximityoff);
                }

            }

            holder.imgNodesBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1 = "on";
                    if (mFavoriteModel.getStatus().trim().equals("off")) {
                        payload1 = "on";
                    } else {
                        payload1 = "off";
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String ip_devices = sharedPref.getString("IPaddress", "127.0.0.1");
                    String ssid_devices = sharedPref.getString("WiFi_SSID", "None");

                    ip_nodes = "";
                    wifi_name = "";

                    nodesInstalled.clear();
                    nodesInstalled.addAll(mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()));
                    int countDB = mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()).size();
                    if (countDB != 0) {
                        for (int i = 0; i < countDB; i++) {
                            ip_nodes = nodesInstalled.get(i).getLocalip();
                            wifi_name = nodesInstalled.get(i).getIcon();
                        }
                    }

                    String ipDev = ip_devices.substring(0, 10);
                    String ipNod = ip_nodes.substring(0, 10);
                    Log.d("DEBUG", "WiFi Compare : " + ssid_devices + " : " + wifi_name);

                    if (!mFavoriteModel.getOnline().trim().equals("true")) {
                        TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d() + " Offline, we check this node now" +
                                " by sending a request", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();

                        new getOnline(mFavoriteModel.getNodeid(), v).execute();
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            executeCommandviaInet(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            holder.loading.setVisibility(View.VISIBLE);
                            holder.loading.setScaleY(4f);
                        }

                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server, executing command via local WiFi", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            Toast.makeText(context, "The Node you are trying to execute is not avalaible in current network " +
                                    "Device ID: " + mFavoriteModel.getNodeid() + " Network: " + wifi_name, Toast.LENGTH_SHORT).show();

                        }
                    }

                }
            });

            holder.imgNodesBut.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(mFavoriteModel.getNice_name_d());
                    String nice_name = mFavoriteModel.getNice_name_d();

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                    return false;
                }
            });
        } else if ((mFavoriteModel.getSensor().trim()).equals("temp")) {

            final TempHolder holder = (TempHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());

            String t = mFavoriteModel.getTemp();
            if (t !=null) {
                String t1 = t.substring(0, 2);
                int t2 = Integer.parseInt(t1.replaceAll("[\\D]", ""));
                holder.temp.setText(t2 - 7 + "°C");
            }

            String h = mFavoriteModel.getHum();
            if (h !=null) {
                String h1 = h.substring(0, 2);
                int h2 = Integer.parseInt(h1.replaceAll("[\\D]", ""));
                holder.hum.setText(h2 + "%");
            }

            if (mFavoriteModel.getStatus().trim().equals("off")) {
                holder.imgNode.setImageResource(R.drawable.offlamp1);
                holder.loading.setVisibility(View.GONE);

            } else {
                holder.imgNode.setImageResource(R.drawable.onlamp1);
                holder.loading.setVisibility(View.GONE);

            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.on_indicator);
            } else {
                holder.imgOnline.setImageResource(R.drawable.off_indicator);
            }

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String payload1 = "on";
                    if (mFavoriteModel.getStatus().trim().equals("off")) {
                        payload1 = "on";
                    } else {
                        payload1 = "off";
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String ip_devices = sharedPref.getString("IPaddress", "127.0.0.1");
                    String ssid_devices = sharedPref.getString("WiFi_SSID", "None");

                    ip_nodes = "";
                    wifi_name = "";

                    nodesInstalled.clear();
                    nodesInstalled.addAll(mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()));
                    int countDB = mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()).size();
                    if (countDB != 0) {
                        for (int i = 0; i < countDB; i++) {
                            ip_nodes = nodesInstalled.get(i).getLocalip();
                            wifi_name = nodesInstalled.get(i).getIcon();
                        }
                    }

                    String ipDev = ip_devices.substring(0, 10);
                    String ipNod = ip_nodes.substring(0, 10);
                    Log.d("DEBUG", "WiFi Compare : " + ssid_devices + " : " + wifi_name);

                    if (!mFavoriteModel.getOnline().trim().equals("true")) {
                        TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d() + " Offline, we check this node now" +
                                " by sending a request", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();

                        new getOnline(mFavoriteModel.getNodeid(), v).execute();
                    }

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            executeCommandviaInet(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            holder.loading.setVisibility(View.VISIBLE);
                            holder.loading.setScaleY(4f);
                        }

                    } else {
                        TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server, executing command via local WiFi", TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        if (ssid_devices.equals(wifi_name)) {
                            if (ipDev.equals(ipNod)) {
                                executeCommandviaLocal(mFavoriteModel.getNodeid(), mFavoriteModel.getChannel(),payload1);
                            }
                        } else {
                            Toast.makeText(context, "The Node you are trying to execute is not avalaible in current network " +
                                    "Device ID: " + mFavoriteModel.getNodeid() + " Network: " + wifi_name, Toast.LENGTH_SHORT).show();

                        }
                    }

                }
            });


            holder.imgNode.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(mFavoriteModel.getNice_name_d());
                    String nice_name = mFavoriteModel.getNice_name_d();

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return false;
                }
            });
        } else if ((mFavoriteModel.getSensor().trim()).equals("0")) {

            final ButtonHolder holder = (ButtonHolder) viewHolder;

            holder.node_name.setText(mFavoriteModel.getNice_name_d());

            String lastval=mFavoriteModel.getStatus();

            if (lastval!=null && !lastval.equals("")) {
                    holder.imgNode.setImageResource(R.drawable.videcall);


            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.on_indicator);

            } else {
                holder.imgOnline.setImageResource(R.drawable.off_indicator);
            }

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    Random r = new Random();
                    int i1 = r.nextInt(80 - 65) + 65;
                    if (mStatusServer) {
                        if (mFavoriteModel.getOnline().trim().equals("true")) {
                            String topic = "devices/" + mFavoriteModel.getNodeid() + "/$calling";
                            String payload = "true-" + i1;
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

                            Intent i = new Intent(context, ConnectActivity.class);
                            i.putExtra("node_id", mFavoriteModel.getNodeid()+i1);
                            context.startActivity(i);

                        }else {
                            TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d()+" Offline, we check this node now" +
                                    " by sending a request", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                            new getOnline(mFavoriteModel.getNodeid(),v).execute();
                        }
                    } else {
                        Toast.makeText(context,"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });

            holder.imgNode.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(mFavoriteModel.getNice_name_d());
                    String nice_name = mFavoriteModel.getNice_name_d();

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return false;
                }
            });

        } else if ((mFavoriteModel.getSensor().trim()).equals("rgb")) {

            final RGBHolder holder = (RGBHolder) viewHolder;
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String ip_devices = sharedPref.getString("IPaddress", "127.0.0.1");

            nodesInstalled.clear();
            nodesInstalled.addAll(mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()));
            int countDB = mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()).size();

            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    ip_nodes = nodesInstalled.get(i).getLocalip();
                }
            }


            String ipDev = ip_devices.substring(0,10);
            String ipNod = ip_nodes.substring(0,10);
            //Log.d("DEBUG", "onBindViewHolder: "+ipDev+" ipNode "+ipNod);

            holder.node_name.setText(mFavoriteModel.getNice_name_d());

            holder.imgNode.setImageResource(R.mipmap.smartrgb);

            String convRGB = mFavoriteModel.getStatus();
            //Log.d("DEBUG", "rgb: "+convRGB);
            int colorVal = 0;
            if (convRGB.length()>10) {
                String[] a = convRGB.split(",");
                int redval = Integer.parseInt(a[0].substring(4));
                int greenVal = Integer.parseInt(a[1]);
                int blueVal = Integer.parseInt(a[2]);

                //int blueVal = Integer.parseInt(a[2].substring(0, a[2].length()-1));
                colorVal = Color.rgb(redval, greenVal, blueVal);
            }

            holder.imgNode.setColorFilter(colorVal, PorterDuff.Mode.SRC_ATOP);

            String lastval=mFavoriteModel.getStatus();
            if (lastval!=null && !lastval.equals("")) {
                holder.imgNode.setImageResource(R.mipmap.smartrgb);

            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.on_indicator);

            } else {
                holder.imgOnline.setImageResource(R.drawable.off_indicator);
            }

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);

                    //if (mStatusServer) {
                        if (!mFavoriteModel.getOnline().trim().equals("true")) {
                            new getOnline(mFavoriteModel.getNodeid(),v).execute();
                        }
                        ColorPickerDialogBuilder
                                    .with(context)
                                    .setTitle("Choose color")
                                    .initialColor(Color.WHITE)
                                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                    .density(12)
                                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                                        @Override
                                        public void onColorSelected(int selectedColor) {

                                        }
                                    })
                                    .setPositiveButton("ok", new ColorPickerClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                            //changeBackgroundColor(selectedColor);
                                            int red = Color.red(selectedColor);
                                            int green = Color.green(selectedColor);
                                            int blue = Color.blue(selectedColor);

                                            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                            String ip_devices = sharedPref.getString("IPaddress", "127.0.0.1");
                                            String ssid_devices = sharedPref.getString("WiFi_SSID", "None");

                                            ip_nodes = "";
                                            wifi_name = "";

                                            nodesInstalled.clear();
                                            nodesInstalled.addAll(mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()));
                                            int countDB = mDbNodeRepo.getNodeListbyNode(mFavoriteModel.getNodeid()).size();
                                            if (countDB != 0) {
                                                for (int i = 0; i < countDB; i++) {
                                                    ip_nodes = nodesInstalled.get(i).getLocalip();
                                                    wifi_name = nodesInstalled.get(i).getIcon();
                                                }
                                            }

                                            String ipDev = ip_devices.substring(0, 10);
                                            String ipNod = ip_nodes.substring(0, 10);
                                            Log.d("DEBUG", "WiFi Compare : " + ssid_devices + " : " + wifi_name);

                                            if (!mFavoriteModel.getOnline().trim().equals("true")) {
                                                TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d() + " Offline, we check this node now" +
                                                        " by sending a request", TSnackbar.LENGTH_LONG);
                                                View snackbarView = snackbar.getView();
                                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                                snackbar.show();

                                                new getOnline(mFavoriteModel.getNodeid(), v).execute();
                                            }

                                            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                            mStatusServer = sharedPref.getBoolean("conStatus", false);
                                            if (mStatusServer) {
                                                if (ssid_devices.equals(wifi_name)) {
                                                    if (ipDev.equals(ipNod)) {
                                                        udpClientThread = new UdpClientThread(
                                                                "rgb(" + red + "," + green + "," + blue +",0)",
                                                                ip_nodes,
                                                                4210,
                                                                udpClientHandler);
                                                        udpClientThread.start();
                                                    }
                                                } else {

                                                        String topic = "devices/" + mFavoriteModel.getNodeid() + "/led/color/set";
                                                        String payload = "rgb(" + red + "," + green + "," + blue + ",0)";
                                                        byte[] encodedPayload = new byte[0];
                                                        try {
                                                            encodedPayload = payload.getBytes("UTF-8");
                                                            MqttMessage message = new MqttMessage(encodedPayload);
                                                            message.setQos(1);
                                                            message.setRetained(true);
                                                            Connection.getClient().publish(topic, message);
                                                            Toast.makeText(context, "sending via Internet", Toast.LENGTH_SHORT).show();

                                                        } catch (UnsupportedEncodingException | MqttException e) {
                                                            e.printStackTrace();
                                                        }

                                                }

                                            } else {
                                                TSnackbar snackbar = TSnackbar.make(v, "You dont connect to server, executing command via local WiFi", TSnackbar.LENGTH_LONG);
                                                View snackbarView = snackbar.getView();
                                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                                snackbar.show();
                                                Intent intent = new Intent("addNode");
                                                intent.putExtra("Connect", "con");
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                if (ssid_devices.equals(wifi_name)) {
                                                    if (ipDev.equals(ipNod)) {
                                                        udpClientThread = new UdpClientThread(
                                                                "rgb(" + red + "," + green + "," + blue +",0)",
                                                                ip_nodes,
                                                                4210,
                                                                udpClientHandler);
                                                        udpClientThread.start();
                                                    }
                                                } else {
                                                    Toast.makeText(context, "The Node you are trying to execute is not avalaible in current network " +
                                                            "Device ID: " + mFavoriteModel.getNodeid() + " Network: " + wifi_name, Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .build()
                                    .show();

                        //}else {
                           /* TSnackbar snackbar = TSnackbar.make(v, mFavoriteModel.getNice_name_d()+" Offline, we check this node now" +
                                    " by sending a request", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                            new getOnline(mFavoriteModel.getNodeid(),v).execute();*/
                       // }
                    //} else {
                    //    Toast.makeText(context,"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    //    Intent intent = new Intent("addNode");
                    //    intent.putExtra("Connect", "con");
                    //    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    //}
                }
            });

            holder.imgNode.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(mFavoriteModel.getNice_name_d());
                    String nice_name = mFavoriteModel.getNice_name_d();

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItem(position);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return false;
                }
            });

        }


    }

    private class getOnline extends AsyncTask<Void, Integer, String> {
        String idnode;
        View rootView;
        InstalledNodeModel installedNodeModel;
        dbNodeRepo mDbNodeRepo;

        public getOnline(String nodeid, View v) {
            this.idnode = nodeid;
            this.rootView = v;
        }

        protected void onPreExecute (){
            mDbNodeRepo = new dbNodeRepo(context);
            installedNodeModel = new InstalledNodeModel();
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DEBUG", "doing http GET: ");

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(context);
                final String urlGet = "http://cloud.olmatix.com:1880/API/GET/DEVICE?id=" + idnode;
                Log.d("DEBUG", "doInBackground: "+urlGet);

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, urlGet,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.d("DEBUG", "onResponse: " + idnode + " response "+  response);
                                String result = response.trim();
                                if (result.equals("false")){
                                    Log.d("DEBUG", "FALSE: "+response);
                                    //Toast.makeText(context, idnode +" is definetly OFFLINE, please check it",Toast.LENGTH_SHORT).show();

                                    TSnackbar snackbar = TSnackbar.make(rootView, idnode+" is definetly OFFLINE, please check it" +
                                            " by looking at Blue Led indicator, does it blink?", TSnackbar.LENGTH_LONG);
                                    View snackbarView = snackbar.getView();
                                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                    snackbar.show();
                                } else {
                                    installedNodeModel.setOnline("true");
                                    installedNodeModel.setNodesID(idnode);
                                    mDbNodeRepo.updateOnline(installedNodeModel);
                                    TSnackbar snackbar = TSnackbar.make(rootView, idnode+" is ONLINE, refreshing now", TSnackbar.LENGTH_LONG);
                                    View snackbarView = snackbar.getView();
                                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                    snackbar.show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);

            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer...a){
//            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            //notifyChange();
            Intent intent = new Intent("MQTTStatusDetail");
            intent.putExtra("NotifyChangeDetail", "2");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

    class ButtonHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline;
        public ImageButton imgNode;
        public ProgressBar loading;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            loading = (ProgressBar)view.findViewById(R.id.pbProcessing);

        }

    }

    class StatusHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_names, status;
        public ImageView imgNodes, imgOnline;
        public ImageButton imgNodesBut;
        public ProgressBar loading;


        public StatusHolder(View view) {
            super(view);
            imgNodes = (ImageView) view.findViewById(R.id.icon_node);
            imgNodesBut = (ImageButton) view.findViewById(R.id.icon_node_button);
            node_names = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            loading = (ProgressBar)view.findViewById(R.id.pbProcessing);


        }
    }

    class TempHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status, temp, hum;
        public ImageView imgOnline;
        public ImageButton imgNode;
        public ProgressBar loading;


        public TempHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            temp = (TextView) view.findViewById(R.id.temp);
            hum = (TextView) view.findViewById(R.id.hum);
            loading = (ProgressBar)view.findViewById(R.id.pbProcessing);

        }
    }

    class PhoneHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline;
        public ImageButton imgNode;

        public PhoneHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
        }
    }

    class RGBHolder extends NodeDashboardAdapter.ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline;
        public ImageButton imgNode;

        public RGBHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
        }

    }

    private void saveOnTime(String nodeid, String ch, String mMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                //Log.d(TAG, "run ON: "+Channel);
                durationModel.setNodeId(nodeid);
                durationModel.setChannel(ch);
                durationModel.setStatus(mMessage);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                durationModel.setTimeStampOn(now.getTimeInMillis());
                durationModel.setTimeStampOff(Long.valueOf("0"));

                mDbNodeRepo.insertDurationNode(durationModel);
            }
        });
    }

    private void saveOffTime(String nodeid, String ch, String mMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                long dura;
                durationModel.setNodeId(nodeid);
                durationModel.setChannel(ch);
                durationModel.setStatus(mMessage);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                durationModel.setTimeStampOff(now.getTimeInMillis());
                if(durationModel.getTimeStampOn()!=null) {

                    dura = (now.getTimeInMillis() - durationModel.getTimeStampOn())/1000;
                    if (dura<25292000) {
                        durationModel.setDuration(dura);
                    } else {
                        durationModel.setDuration(Long.valueOf(0));
                    }

                }
                mDbNodeRepo.updateOff(durationModel);

                data3.addAll(mDbNodeRepo.getNodeUpdateZero());
                int countDB = mDbNodeRepo.getNodeUpdateZero().size();
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        if (data3.get(i).getTimeStampOn() != null) {
                            dura = data3.get(i).getTimeStampOff()-data3.get(i).getTimeStampOn();
                            int id = data3.get(i).getId();
                            durationModel.setId(id);
                            durationModel.setDuration(dura/1000);
                            mDbNodeRepo.updateOffbyID(durationModel);

                        }
                    }
                }
                data3.clear();
            }
        });
    }

    public class UdpClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private NodeDashboardAdapter parent;

        public UdpClientHandler(NodeDashboardAdapter parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    private void updateState(String state){
        Toast.makeText(context, "Update state "+state, Toast.LENGTH_SHORT).show();
    }

    private void updateRxMsg(String rxmsg){
        //Toast.makeText(context, rxmsg, Toast.LENGTH_SHORT).show();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (!mStatusServer) {

            String[] a = rxmsg.split(",");
            String mNiceName, lastValue;
            data1.clear();
            data1.addAll(mDbNodeRepo.getNodeDetail(a[0], a[1]));
            int countDB = mDbNodeRepo.getNodeDetail(a[0], a[1]).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    if (data1.get(i).getNice_name_d() != null) {
                        mNiceName = data1.get(i).getNice_name_d();
                    } else {
                        mNiceName = data1.get(i).getName();
                    }
                    lastValue = data1.get(i).getStatus();
                    if (TextUtils.isEmpty(lastValue)) {
                        lastValue = "off";
                    }

                    Log.d("DEBUG", "from Local: " + lastValue +" "+a[2]);

                    if (a[2].equals("on")) {
                        //if (!lastValue.equals("on")) {
                            detailNodeModel.setStatus(a[2]);
                            saveOnTime(a[0], a[1], a[2]);
                            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                            dbnode.setTopic(mNiceName + " is " + "ON");
                            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                            //dbnode.setNode_id(a[0]);
                            //dbnode.setChannel(a[1]);
                            detailNodeModel.setNode_id(a[0]);
                            detailNodeModel.setChannel(a[1]);
                            mDbNodeRepo.insertDbMqtt(dbnode);
                            mDbNodeRepo.update_detail(detailNodeModel);

                        //}

                    } else if (a[2].equals("off")) {
                        //if (!lastValue.equals("off")) {
                            detailNodeModel.setStatus(a[2]);
                            saveOffTime(a[0], a[1], a[2]);
                            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                            dbnode.setTopic(mNiceName + " is " + "OFF");
                            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                            detailNodeModel.setNode_id(a[0]);
                            detailNodeModel.setChannel(a[1]);
                            mDbNodeRepo.insertDbMqtt(dbnode);
                            mDbNodeRepo.update_detail(detailNodeModel);
                            detailNodeModel.setStatus_theft("false");
                            mDbNodeRepo.update_detailSensorTheft(detailNodeModel);
                        //}
                    }
                }
            }
        }
        NodeDashboardAdapter.this.notifyDataSetChanged();
    }

    private void clientEnd(){
        udpClientThread = null;
        //Toast.makeText(context, "Update state"+"clientEnd()", Toast.LENGTH_SHORT).show();

    }

    private void executeCommandviaInet (String nodeid, String channel, String command){
        //Toast.makeText(context, "sending command via Internet for " +nodeid, Toast.LENGTH_SHORT).show();
        String topic = "devices/" + nodeid + "/light/on_" + channel + "/set";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = command.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setQos(1);
            message.setRetained(true);
            Connection.getClient().publish(topic, message);
            Log.d("DEBUG", "onClick: " + topic + " --> " + message);


        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void executeCommandviaLocal (String nodeid, String channel, String command){
        //Toast.makeText(context, "sending command via Local WiFi for " +nodeid, Toast.LENGTH_SHORT).show();
        udpClientThread = new UdpClientThread(
                channel + "," + command,
                ip_nodes,
                4210,
                udpClientHandler);
        udpClientThread.start();
    }
}