package com.olmatix.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.olmatix.helper.CustomVolleyRequest;
import com.olmatix.lesjaw.olmatix.R;

import java.util.ArrayList;

/**
 * Created by Lesjaw on 08/01/2017.
 */

public class GridViewAdapter extends BaseAdapter {

    //Imageloader to load images
    private ImageLoader imageLoader;

    //Context
    private Context context;

    //Array List that would contain the urls and the titles for the images
    private ArrayList<String> images;
    private ArrayList<String> names;

    public GridViewAdapter (Context context, ArrayList<String> images, ArrayList<String> names){
        //Getting all the values
        this.context = context;
        this.images = images;
        this.names = names;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Creating a linear layout
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //NetworkImageView
        NetworkImageView networkImageView = new NetworkImageView(context);

        //Initializing ImageLoader
        imageLoader = CustomVolleyRequest.getInstance(context).getImageLoader();
        imageLoader.get(images.get(position), ImageLoader.getImageListener(networkImageView,
                    R.mipmap.olmatixlogo, android.R.drawable.ic_dialog_alert));


        //Setting the image url to load
        networkImageView.setImageUrl(images.get(position),imageLoader);


        //Creating a textview to show the title
        TextView textView = new TextView(context);
        textView.setMaxLines(1);
        textView.setText(names.get(position));

        //Scaling the imageview
        networkImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        networkImageView.setLayoutParams(new GridView.LayoutParams(200,200));

        //Adding views to the layout
        linearLayout.addView(textView);
        linearLayout.addView(networkImageView);

        networkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imagesUrl = images.get(position);
                Log.d("DEBUG", "onClick: "+images.get(position)+ " "+ names.get(position));
                Intent intent = new Intent("loadcontent");
                intent.putExtra("imagesUrl", imagesUrl);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        //Returnint the layout
        return linearLayout;
    }
}
