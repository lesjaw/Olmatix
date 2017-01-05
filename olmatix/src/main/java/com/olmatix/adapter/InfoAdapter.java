package com.olmatix.adapter;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DurationModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rahman on 12/27/2016.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    public static final int mBUTTON = 0;
    public static final int mLOCATION = 1;
    private static dbNodeRepo DbNodeRepo;
    private static ArrayList<DurationModel> data ;
    private int[] mDataSetTypes;
    Context context;
    String loc = null;
    String distance;
    private String z;


    public InfoAdapter(String distance, int[] mDataTypes, Context context, OnStartDragListener mDragStartListener) {
        this.context = context;
        this.mDataSetTypes = mDataTypes;
        this.mDragStartListener = mDragStartListener;
        this.distance = distance;
    }

    @Override
    public int getItemViewType(int viewType) {
        return mDataSetTypes[viewType];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == mBUTTON) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_info_button, viewGroup, false);
            v.setMinimumWidth(viewGroup.getMeasuredWidth());
            v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));

            return new ButtonInfoHolder(v);

        }

        if (viewType == mLOCATION) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_info_location, viewGroup, false);
            v.setMinimumWidth(viewGroup.getMeasuredWidth());
            v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));

            return new LocationInfoHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        if (viewHolder.getItemViewType() == mBUTTON) {
            final ButtonInfoHolder holder = (ButtonInfoHolder) viewHolder;
            ArrayList<Entry> yVals = setYAxisValues();
            ArrayList<String> xVals = setXAxisValues();

            LineDataSet set1, set2;

            XAxis xAxis = holder.mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawAxisLine(false);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(Color.rgb(255, 192, 56));
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f); // one hour
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                private SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yy");

                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    long millis = System.currentTimeMillis();
                    return mFormat.format(new Date(millis));
                }
            });


            YAxis leftAxis = holder.mChart.getAxisLeft();
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(24f);
            leftAxis.setTextColor(Color.BLACK);

            YAxis rightAxis = holder.mChart.getAxisRight();
            rightAxis.setEnabled(false);


            Log.d("DEBUG", "onBindViewHolder: " +z);
            set1 = new LineDataSet(yVals, getNodeIdData(z));
            set1.setFillAlpha(110);
            set1.setColor(Color.BLUE);
            set1.setCircleColor(Color.GREEN);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);

/*
            set2 = new LineDataSet(yVals, "TEST SET ");
            set2.setFillAlpha(110);
            set2.setColor(Color.BLACK);
            set2.setCircleColor(Color.YELLOW);
            set2.setLineWidth(1f);
            set2.setCircleRadius(3f);
            set2.setDrawCircleHole(false);
            set2.setValueTextSize(9f);
            set2.setDrawFilled(true);
*/
            // no description text
            holder.mChart.getDescription().setEnabled(false);

            holder.mChart.animateX(2500);

            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            holder.mChart.setData(data);
            holder.mChart.invalidate();


        } else if (viewHolder.getItemViewType() == mLOCATION) {
            final LocationInfoHolder holder = (LocationInfoHolder) viewHolder;
            PreferenceHelper mPrefHelper = new PreferenceHelper(context.getApplicationContext());
            double mLat = mPrefHelper.getHomeLatitude();
            double mLong = mPrefHelper.getHomeLongitude();
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String adString = "";

            List<Address> list;
            try {
                list = geocoder.getFromLocation(mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), 1);
                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    loc = address.getLocality();
                    if (address.getAddressLine(0) != null)
                        adString = ", " + address.getAddressLine(0);
                }
            } catch (IOException e) {
                Log.e("DEBUG", "LOCATION ERR:" + e.getMessage());
            }

            if (loc==null){
                holder.location.setText("No location set");
                holder.distance.setText("No location found");
            } else {
                holder.location.setText("at " + loc + adString + " | " + String.valueOf((Double) mLat) + " : " + String.valueOf((Double) mLong));
                if (distance==null) {
                    holder.distance.setText("you are at unknown from home, waiting update");
                } else {
                    holder.location.setSelected(true);
                    holder.distance.setText("you are at " + distance + " from home");
                }
            }
        }
    }

    private String getNodeIdData( String z) {
        DbNodeRepo = new dbNodeRepo(context);
        int countDb = DbNodeRepo.getChartDurationList().size();
        data = new ArrayList<>();
        data.addAll(DbNodeRepo.getChartDurationList());
        for (int k = 0; k < data.size(); k++) {
            z = data.get(k).getNiceName();
            Log.d("DEBUG", "nodeIdData 1: " +  data.get(k).getNiceName());

        }

        return z;
    }

    private ArrayList<Entry> setYAxisValues() {
        ArrayList<Entry> yVals1 = null;
        DbNodeRepo = new dbNodeRepo(context);
        int countDb = DbNodeRepo.getChartDurationList().size();
        Log.d("DEBUG", "setYAxis: " +  countDb);
        data = new ArrayList<>();
        data.addAll(DbNodeRepo.getChartDurationList());
        Long[] mDuration = new Long[data.size()];
        for (int i = 0; i < data.size(); i++) {

//            Long mDur = data.get(i).getDuration();

//            Long[] arrayOfLongs = data.get(i).getDuration();
            mDuration[i] = data.get(i).getDuration();
            System.out.println(mDuration[i]);
            yVals1 = new ArrayList<Entry>();
            yVals1.add(new Entry(mDuration[i], i));
        }

        return yVals1;
    }

    private ArrayList<String> setXAxisValues(){
        ArrayList<String> xVals = null;
        DbNodeRepo = new dbNodeRepo(context);
        int countDb = DbNodeRepo.getChartDurationList().size();
        data = new ArrayList<>();
        data.addAll(DbNodeRepo.getChartDurationList());
        Long[] mTimeOn = new Long[data.size()];
        for (int i = 0; i < data.size(); i++) {

            System.out.println(mTimeOn[i]);
            SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yy");
            mTimeOn[i]  = data.get(i).getTimeStampOn();
            try {

                String[] mDate = new String[Integer.parseInt(mFormat.format(new Date(mTimeOn[i])))];
                Log.d("DEBUG", "setXAxis UN CONVERT: " +  mTimeOn[i]);
                Log.d("DEBUG", "setXAxis Format: " +  mDate[i]);
                xVals = new ArrayList<String>();
                xVals.add(mDate[i]);

            } catch( NumberFormatException nfe) {

            }


        }

        return xVals;
    }

    @Override
    public int getItemCount() {
        return mDataSetTypes.length;
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

    public void notifyDistance(String Dist) {
        this.distance = Dist;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class ButtonInfoHolder extends ViewHolder {
        public LineChart mChart;

        public ButtonInfoHolder(View view) {
            super(view);
            mChart = (LineChart) view.findViewById(R.id.chart);

        }
    }

    public class LocationInfoHolder extends ViewHolder {
        public TextView location, distance;
        public ImageView imgNodes;

        public LocationInfoHolder(View view) {
            super(view);
            location = (TextView) view.findViewById(R.id.location);
            distance = (TextView) view.findViewById(R.id.distance);

        }
    }

}
