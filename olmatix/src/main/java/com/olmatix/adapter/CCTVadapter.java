package com.olmatix.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.CCTVModel;
import com.olmatix.ui.activity.CCTVActivity;
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
    int countdb;

    public CCTVadapter(ArrayList<CCTVModel> data, GatewayActivity gatewayActivity) {
        this.context = gatewayActivity;
        this.cctvModel = data;
    }


    @Override
    public int getCount() {
        countdb = cctvModel.size();
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
        RelativeLayout linearLayout = new RelativeLayout(context);
        //linearLayout.setPadding(20,20,20,20);
        //linearLayout.setOrientation(LinearLayout.VERTICAL);
        //Returnint the layout
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        int mVideoWidth = (int) (dpWidth);
        int mVideoHeight = (int) (dpHeight);

        // get screen size
        int w = mVideoWidth;
        int h = mVideoHeight;

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        ImageView img = new ImageView(context);
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = null;
        if (countdb>1) {
           layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    h/2);
        } else {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    h);
        }
        RelativeLayout.LayoutParams layoutParamsicon = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams layoutParamsName = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        layoutParams.gravity = Gravity.CENTER;
        layoutParamsicon.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParamsicon.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParamsName.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParamsName.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParamsName.setMargins(10,0,0,10);
        img.setLayoutParams(layoutParams);
        img.setBackgroundColor(Color.BLACK);
        icon.setLayoutParams(layoutParamsicon);
        icon.setImageResource(R.mipmap.smartcctv);
        icon.setBackgroundColor(Color.BLACK);

        //img.getLayoutParams().width = setwd;
        icon.requestLayout();
        img.requestLayout();

        TextView name = new TextView(context);
        name.setMaxLines(1);
        name.setTextSize(20);
        name.setText(cctvModel.get(position).getName());
        name.setTextColor(Color.WHITE);
        name.setPadding(10,5,5,10);
        name.requestLayout();

        TextView IP = new TextView(context);
        IP.setMaxLines(1);
        IP.setTextSize(9);
        IP.setText(cctvModel.get(position).getIp());
        IP.setTextColor(Color.WHITE);
        IP.setPadding(10,5,5,10);
        IP.setLayoutParams(layoutParamsName);
        IP.requestLayout();

        linearLayout.addView(img);
        linearLayout.addView(name);
        linearLayout.addView(IP);
        linearLayout.addView(icon);

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
                        mDbNodeRepo.deleteCCTV(String.valueOf(cctvModel.get(position).getName()));
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
