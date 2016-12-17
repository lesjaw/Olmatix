package com.olmatix.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Favorite_NodeModel;
import com.olmatix.ui.fragment.Favorite_Node;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class NodeFavoriteAdapter extends RecyclerView.Adapter<NodeFavoriteAdapter.ViewHolder> implements ItemTouchHelperAdapter
{

    List<Favorite_NodeModel> nodeList;
    private final OnStartDragListener mDragStartListener;
    Context context;
    String fw_name;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class ButtonHolder extends NodeFavoriteAdapter.ViewHolder {
        public TextView node_name, upTime, status, fwName;
        public ImageButton imgNode;

        public ButtonHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public class StatusHolder extends NodeFavoriteAdapter.ViewHolder {
        public TextView node_name, upTime, status, fwName;
        public ImageButton imgNode;

        public StatusHolder(View view) {
            super(view);
            imgNode = (ImageButton) view.findViewById(R.id.icon_node);
            node_name = (TextView) view.findViewById(R.id.node_name);

        }
    }

    public NodeFavoriteAdapter(List<Favorite_NodeModel> nodeList, String fw_name, OnStartDragListener dragStartListener, Favorite_Node favorite_node) {

        this.nodeList = nodeList;
        mDragStartListener = dragStartListener;
        this.fw_name = fw_name;

        this.context = context;


    }
    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    @Override
    public NodeFavoriteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;


        if (fw_name.equals("light")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_button, parent, false);

            return new NodeFavoriteAdapter.ButtonHolder(itemView);

        }
        else if(fw_name.equals("sensor"))
        {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.frag_node_sensor, parent, false);

            return new NodeFavoriteAdapter.StatusHolder(itemView);
        }

        return null;
    }


    public void onBindViewHolder(final NodeFavoriteAdapter.ViewHolder viewHolder, final int position) {
        //final int pos = position;
        final Favorite_NodeModel mFavoriteModel = nodeList.get(position);
        if(fw_name.equals("ligth")) {

            //Toast.makeText(context,"I m in",Toast.LENGTH_LONG).show();
            final NodeFavoriteAdapter.ButtonHolder holder = (NodeFavoriteAdapter.ButtonHolder) viewHolder;

            holder.fwName.setText(mFavoriteModel.getFavNodeID());
            holder.imgNode.setImageResource(R.drawable.olmatixlogo);



        }else if(fw_name.equals("sensor"))
        {
            final NodeFavoriteAdapter.StatusHolder holder = (NodeFavoriteAdapter.StatusHolder) viewHolder;

            holder.imgNode.setImageResource(R.drawable.olmatixlogo);


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


}
