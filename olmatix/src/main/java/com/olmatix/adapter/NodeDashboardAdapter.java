package com.olmatix.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.ui.fragment.DashboardNode;
import com.olmatix.utils.Connection;

import org.appspot.olmatixrtc.ConnectActivity;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeDashboardAdapter extends RecyclerView.Adapter<NodeDashboardAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    //private final OnStartDragListener mDragStartListener;
    List<DashboardNodeModel> nodeList;
    private Animation animConn;
    Context context;
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    public NodeDashboardAdapter(ArrayList<DashboardNodeModel> nodeList, Context dashboardnode, DashboardNode dashboardNode) {
        this.nodeList = nodeList;
        //mDragStartListener = dragStartListener;
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
        }

        return viewType;
    }

    public void notifyChange() {
        notifyDataSetChanged();
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
            case 2:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_temp, viewGroup, false);

                return new TempHolder(v);
            case 3:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_dash_phone, viewGroup, false);

                return new PhoneHolder(v);
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
                if (mFavoriteModel.getStatus().trim().equals("false")) {
                    holder.imgNode.setImageResource(R.drawable.offlamp1);
                    holder.imgSending.setVisibility(View.GONE);

                } else {
                    holder.imgNode.setImageResource(R.drawable.onlamp1);
                    holder.imgSending.setVisibility(View.GONE);

                }
            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
                holder.imgNode.setBackgroundColor(Color.WHITE);
                holder.iconstat.setVisibility(View.GONE);

            } else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
                holder.imgNode.setBackgroundColor(Color.parseColor("#A9A9A9"));
                holder.iconstat.setVisibility(View.VISIBLE);
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

            if ((mFavoriteModel.getStatus().trim()).equals("true")) {
                holder.imgNodesBut.setImageResource(R.drawable.onsec);

            } else {
                holder.imgNodesBut.setImageResource(R.drawable.offsec);
            }

            String theft = mFavoriteModel.getStatus_theft();
            if (theft.equals("true")){
                holder.imgNodesBut.setImageResource(R.drawable.theft);
            }

            String lastval=mFavoriteModel.getOnline();
            if (lastval!=null && !lastval.equals("")) {
                if (mFavoriteModel.getOnline().trim().equals("true")) {
                    holder.imgOnline.setImageResource(R.drawable.ic_check_green);
                    holder.imgNodesBut.setBackgroundColor(Color.WHITE);
                    holder.iconstat.setVisibility(View.GONE);
                } else {
                    holder.imgOnline.setImageResource(R.drawable.ic_check_red);
                    holder.imgNodesBut.setBackgroundColor(Color.parseColor("#A9A9A9"));
                    holder.iconstat.setVisibility(View.VISIBLE);
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

            if (mFavoriteModel.getStatus().trim().equals("false")) {
                holder.imgNode.setImageResource(R.drawable.offlamp1);
                holder.imgSending.setVisibility(View.GONE);

            } else {
                holder.imgNode.setImageResource(R.drawable.onlamp1);
                holder.imgSending.setVisibility(View.GONE);

            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
                holder.imgNode.setBackgroundColor(Color.WHITE);
                holder.iconstat.setVisibility(View.GONE);
            } else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
                holder.imgNode.setBackgroundColor(Color.parseColor("#A9A9A9"));
                holder.iconstat.setVisibility(View.VISIBLE);
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
                    holder.imgSending.setVisibility(View.GONE);


            }
            if (mFavoriteModel.getOnline().trim().equals("true")) {
                holder.imgOnline.setImageResource(R.drawable.ic_check_green);
                holder.imgNode.setBackgroundColor(Color.WHITE);
                holder.iconstat.setVisibility(View.GONE);

            } /*else {
                holder.imgOnline.setImageResource(R.drawable.ic_check_red);
                holder.imgNode.setBackgroundColor(Color.parseColor("#A9A9A9"));
                holder.iconstat.setVisibility(View.VISIBLE);
            }*/

            holder.imgNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    Random r = new Random();
                    int i1 = r.nextInt(80 - 65) + 65;
                    if (mStatusServer) {

                        String topic = "devices/" + mFavoriteModel.getNodeid() + "/$calling";
                        String payload = "true-"+i1;
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
                        Toast.makeText(context,"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent("addNode");
                        intent.putExtra("Connect", "con");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                    Intent i = new Intent(context, ConnectActivity.class);
                    i.putExtra("node_id", mFavoriteModel.getNodeid()+i1);
                    context.startActivity(i);
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
        public ImageView iconstat;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            imgSending = (ImageView) view.findViewById(R.id.icon_sending);
            iconstat = (ImageView) view.findViewById(R.id.icon_stat);
        }
    }

    public class StatusHolder extends ViewHolder {
        public TextView node_names, status;
        public ImageView imgNodes, imgOnline,iconstat;
        public ImageButton imgNodesBut;

        public StatusHolder(View view) {
            super(view);
            imgNodes = (ImageView) view.findViewById(R.id.icon_node);
            imgNodesBut = (ImageButton) view.findViewById(R.id.icon_node_button);
            node_names = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            iconstat = (ImageView) view.findViewById(R.id.icon_stat);


        }
    }

    public class TempHolder extends ViewHolder {
        public TextView node_name, status, temp, hum;
        public ImageView imgOnline, imgSending,iconstat;
        public ImageButton imgNode;

        public TempHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            imgSending = (ImageView) view.findViewById(R.id.icon_sending);
            temp = (TextView) view.findViewById(R.id.temp);
            hum = (TextView) view.findViewById(R.id.hum);
            iconstat = (ImageView) view.findViewById(R.id.icon_stat);

        }
    }

    public class PhoneHolder extends ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline, imgSending,iconstat;
        public ImageButton imgNode;

        public PhoneHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);
            imgSending = (ImageView) view.findViewById(R.id.icon_sending);
            iconstat = (ImageView) view.findViewById(R.id.icon_stat);

        }
    }

}