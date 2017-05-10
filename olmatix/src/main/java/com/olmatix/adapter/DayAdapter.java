package com.olmatix.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.olmatix.helper.DayInterface;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.SceneDetailModel;

import java.util.ArrayList;

import static com.olmatix.lesjaw.olmatix.R.drawable.circle_step_done;

/**
 * Created              : Rahman on 2/18/2017.
 * Date Created         : 2/18/2017 / 10:17 AM.
 * ===================================================
 * Package              : com.olmatix.adapter.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2017 Olmatix.
 */
public class DayAdapter extends BaseAdapter {
    private String TAG = "DayListViewAdapter";
    private Context mContext = null;
    String[] mDayArray = {"S","M", "T", "W", "T", "F", "S"};
    private boolean stateChanged;
    public static LayoutInflater inflater;
    ArrayList<SceneDetailModel> sceneDetailData;
    public static ArrayList<TextView> textDays;


    public DayAdapter(Context context, ArrayList<SceneDetailModel> listSceneData) {
        mContext = context;
        this.sceneDetailData = listSceneData;
        textDays = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return  mDayArray.length;
    }

    @Override
    public Object getItem(int position) {
        return mDayArray[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder mHolder;
        View mView = convertView;
        if (mView == null){
            mView = inflater.inflate(R.layout.days, null);
            mHolder = new Holder();
            mHolder.mDayText = (TextView) mView.findViewById(R.id.day);
            textDays.add(mHolder.mDayText = (TextView) mView.findViewById(R.id.day));

            mView.setTag(mHolder);

        }else {
            mHolder = (Holder) mView.getTag();
        }

        mHolder.mDayText.setText(mDayArray[position]);





        mView.setLayoutParams(new ViewGroup.LayoutParams(110, 100));

        return mView;
    }



    class Holder {
        TextView mDayText;
    }
}
