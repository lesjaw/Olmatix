package com.olmatix.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.olmatix.helper.ItemTouchHelperAdapter;

/**
 * Created by Lesjaw on 01/01/2017.
 */

public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    @Override
    public SceneAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(SceneAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
