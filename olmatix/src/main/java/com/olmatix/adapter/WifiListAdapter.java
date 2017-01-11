package com.olmatix.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.olmatix.lesjaw.olmatix.R;

import java.util.List;

/**
 * Created by Rahman on 1/11/2017.
 */

public class WifiListAdapter extends BaseAdapter {
    List<ScanResult> wifiList;
    Context context;
    LayoutInflater inflater;


    public WifiListAdapter(Context context, List<ScanResult> wifiList) {
        this.context = context;
        this.wifiList = wifiList;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.d("DEBUG", "getCount: "+wifiList.size());
        return wifiList.size();

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        System.out.println("viewpos" + position);
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.wifi_item, null);
            holder = new Holder();
            holder.txWifi = (TextView) view.findViewById(R.id.wifi);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.txWifi.setText("SSID :: " + wifiList.get(position).SSID
                + "\nStrength :: " + wifiList.get(position).level);


        return view;
    }

    class Holder {
        TextView txWifi;


    }
}
