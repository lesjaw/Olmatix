package com.olmatix.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.CCTVModel;
import com.olmatix.ui.activity.CCTVActivity;
import com.olmatix.ui.activity.CameraActivity;
import com.olmatix.ui.activity.GatewayActivity;

import java.util.ArrayList;

/**
 * Created by USER on 22/07/2017.
 */

public class CCTVadapter extends BaseAdapter {
    ArrayList<CCTVModel> cctvModel;
    private static LayoutInflater inflater=null;
    private Context context;
    public  static dbNodeRepo mDbNodeRepo;

    public CCTVadapter(ArrayList<CCTVModel> data, GatewayActivity gatewayActivity) {
        this.context = gatewayActivity;
        this.cctvModel = data;
    }


    @Override
    public int getCount() {
        return cctvModel.size();
    }

    @Override
    public Object getItem(int position) {
        return cctvModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        mDbNodeRepo = new dbNodeRepo(context);

        //Creating a linear layout
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(20,20,20,20);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        //Returnint the layout
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int setwd = (int) (dpWidth)+100;
        Log.d("DEBUG", "dpWidth: "+setwd);



        ImageView img = new ImageView(context);

        img.setLayoutParams(new GridView.LayoutParams(setwd,setwd));
        img.setBackgroundColor(Color.BLACK);
        //img.setImageResource(R.mipmap.smartcctv);
        //img.getLayoutParams().width = setwd;
        //img.requestLayout();

        TextView name = new TextView(context);
        name.setMaxLines(1);
        name.setTextSize(20);
        name.setText(cctvModel.get(position).getName());

        TextView IP = new TextView(context);
        IP.setMaxLines(1);
        IP.setTextSize(9);
        IP.setText(cctvModel.get(position).getIp());

        linearLayout.addView(img);
        linearLayout.addView(name);
        linearLayout.addView(IP);

        Log.d("DEBUG", "IP: "+cctvModel.get(position).getIp()+" name "+cctvModel.get(position).getName());

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CCTVActivity.class);
                i.putExtra("nodeid", cctvModel.get(position).getNodeId());
                i.putExtra("nice_name", cctvModel.get(position).getName());
                i.putExtra("ip", cctvModel.get(position).getIp());
                context.startActivity(i);

            }
        });

        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete this CCTV?");
                builder.setMessage(cctvModel.get(position).getName());

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDbNodeRepo.deleteCCTV(String.valueOf(cctvModel.get(position).getNodeId()));
                        Intent intent = new Intent("cctvadapter");
                        intent.putExtra("NotifyChangeDetail", String.valueOf(2));
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

        return linearLayout;
    }
}
