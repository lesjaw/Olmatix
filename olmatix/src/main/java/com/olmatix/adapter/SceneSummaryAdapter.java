package com.olmatix.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneDetailModel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Rahman on 1/3/2017.
 */

public class SceneSummaryAdapter extends BaseAdapter {
    Activity mContext;
    ArrayList<SceneDetailModel> mSceneDetailData;
    private static LayoutInflater mInflater=null;
    SceneDetailModel mSceneDetail;
    private static dbNodeRepo DbNodeRepo;
    private static ArrayList<DetailNodeModel> data ;
    private TextView mId, mTypicalName, sceneCmd;
    private ImageButton imgActions;
    private Boolean mRemove = false;

    public SceneSummaryAdapter(Activity mContext, ArrayList<SceneDetailModel> mSceneDetailData) {
        this.mContext = mContext;
        this.mSceneDetailData = mSceneDetailData;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mSceneDetailData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView = convertView;
        if(convertView == null)
            mView = mInflater.inflate(R.layout.scene_summary_item, null);
        mId             = (TextView) mView.findViewById(R.id.txId);
        mTypicalName    = (TextView) mView.findViewById(R.id.typicalName);
        sceneCmd        = (TextView) mView.findViewById(R.id.scene_command);

        mSceneDetail= mSceneDetailData.get(position);
        for (int i=0; i > mSceneDetailData.size(); i++){
            mId.setText(i);
        }

        mTypicalName.setText(mSceneDetail.getNiceName());
        sceneCmd.setText(mSceneDetail.getCommand());



        return mView;
    }


}
