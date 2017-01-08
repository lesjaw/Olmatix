package com.olmatix.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.olmatix.lesjaw.olmatix.R;

import java.util.ArrayList;

/**
 * Created by Lesjaw on 08/01/2017.
 */

public class iconPickerAdapter extends ArrayAdapter<Integer> {
    Context context;
    int layoutResourceId;
    ArrayList<Integer> data = new ArrayList<>();

    public iconPickerAdapter(Context context, int layoutResourceId, ArrayList<Integer> data) {
        super(context, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.imageItem = (ImageView) row.findViewById(R.id.icon);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        Integer item = data.get(position);
        holder.imageItem.setImageResource(item);

        return row;
    }

    static class RecordHolder {
        ImageView imageItem;

    }


}
