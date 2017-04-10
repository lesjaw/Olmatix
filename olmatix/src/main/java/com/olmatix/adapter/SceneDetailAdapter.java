package com.olmatix.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.ui.activity.scene.ScheduleActivity;

import java.util.ArrayList;

/**
 * Created              : Rahman on 1/5/2017.
 * Date Created         : 1/5/2017 / 3:51 PM.
 * ===================================================
 * Package              : ${PACKAGE_NAME}.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2017 Olmatix.
 */

public class SceneDetailAdapter extends BaseAdapter {
    Activity mContext;
    int layoutResId;
    ArrayList<SceneDetailModel> sceneDetailData;
    private static LayoutInflater inflater=null;
    private ImageButton mImgRemove;
    private TextView mId,mTypicalName, sceneCmd;
    private AlertDialog.Builder mDialog;

    private ScheduleActivity mSceneInput;


    public SceneDetailAdapter(Activity context, ArrayList<SceneDetailModel> listSceneData){
        this.mContext = context;
        this.sceneDetailData = listSceneData;
        this.mSceneInput = (ScheduleActivity) context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    public int getCount() {
        return sceneDetailData.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView == null)
            vi = inflater.inflate(R.layout.scene_summary_item, null);


        mId             = (TextView) convertView.findViewById(R.id.txId);
        mTypicalName    = (TextView) convertView.findViewById(R.id.typicalName);
        sceneCmd        = (TextView) convertView.findViewById(R.id.scene_command);
        mImgRemove      = (ImageButton) convertView.findViewById(R.id.imgActions);


        final SceneDetailModel sceneDetailItem = sceneDetailData.get(position);

        for (int i=0; i > sceneDetailData.size(); i++){
            mId.setText(i);
        }

        mTypicalName.setText(sceneDetailItem.getNiceName());
        sceneCmd.setText(sceneDetailItem.getCommand());

        mImgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog  = new AlertDialog.Builder(v.getContext());
                mDialog.setTitle("Warning..");
                mDialog.setMessage("Are you sure want to remove "+sceneDetailItem.getNiceName()+" items? \n\n this actions cannot be cancelled.");
                mDialog.setIcon(R.drawable.ic_done_green);
                mDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dbNodeRepo mDbNodeRepo = new dbNodeRepo(mContext);
                        mDbNodeRepo.deleteSceneDetailList(sceneDetailItem);
                        mSceneInput.reloadSceneData();
                        dialog.dismiss();

                    }
                });
                mDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        return vi;
    }
}
