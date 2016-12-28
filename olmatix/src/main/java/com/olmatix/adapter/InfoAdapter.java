package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;

/**
 * Created by Rahman on 12/27/2016.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    public static final int mBUTTON = 0;
    public static final int mLOCATION = 1;
    private int mDataTypes[] = {mBUTTON, mLOCATION};
    private String[] ButtonInfo = {"Button 1","Button 2"};
    private String[] LocationInfo = {"Location 1","Location 2", "Location 3"};

    public InfoAdapter(String[] locationInfo, String[] buttonInfo, int[] mDataTypes, OnStartDragListener mDragStartListener) {
        LocationInfo = locationInfo;
        ButtonInfo = buttonInfo;
        this.mDataTypes = mDataTypes;
        this.mDragStartListener = mDragStartListener;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if (viewType == mBUTTON) {
            viewType = 0;

        } else if (viewType == mLOCATION) {
            viewType = 1;
        }

        return viewType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;

        switch (viewType) {
            case 0:

                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_info_button, viewGroup, false);
                v.setMinimumWidth(viewGroup.getMeasuredWidth());

                return new ButtonInfoHolder(v);


            case 1:

                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.frag_info_location, viewGroup, false);

                return new LocationInfoHolder(v);
            default:
        }

        return null;
    }



    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        //final InfoModel mInfoModel = infoList.get(position);
        if (viewHolder.getItemViewType() == mBUTTON){
            final ButtonInfoHolder holder = (ButtonInfoHolder) viewHolder;
        } else if (viewHolder.getItemViewType() == mLOCATION) {
            final LocationInfoHolder holder = (LocationInfoHolder) viewHolder;
        }

    }

    @Override
    public int getItemCount() {
        return mDataTypes.length;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
    //Collections.swap(.length, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class ButtonInfoHolder extends ViewHolder {
        public TextView node_name, status;
        public ImageView imgOnline;
        public ImageView imgNode;

        public ButtonInfoHolder(View view) {
            super(view);
            imgNode = (ImageView) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);

        }
    }

    public class LocationInfoHolder extends ViewHolder {
        public TextView node_names, status;
        public ImageView imgNodes, imgOnline;
        public ImageView imgNodesBut;

        public LocationInfoHolder(View view) {
            super(view);
            imgNodes = (ImageView) view.findViewById(R.id.icon_node);
            imgNodesBut = (ImageView) view.findViewById(R.id.icon_node_button);
            node_names = (TextView) view.findViewById(R.id.node_name);
            imgOnline = (ImageView) view.findViewById(R.id.icon_conn);

        }
    }

}
