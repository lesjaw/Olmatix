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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.ItemTouchHelperAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Duration_Model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rahman on 12/27/2016.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    public static final int mBUTTON = 0;
    public static final int mLOCATION = 1;
    private int[] mDataSetTypes;
    List<Duration_Model> nodeList;
    Context context;
    String loc = null;
    private dbNodeRepo mDbNoderepo;

    public InfoAdapter(ArrayList<Duration_Model>NodeList, int[] mDataTypes,Context context, OnStartDragListener mDragStartListener) {
        this.context=context;
        this.nodeList = NodeList;
        this.mDataSetTypes = mDataTypes;
        this.mDragStartListener = mDragStartListener;
    }

    @Override
    public int getItemViewType(int viewType) {
        return mDataSetTypes[viewType];    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        if (viewType == mBUTTON) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_info_button, viewGroup, false);
            v.setMinimumWidth(viewGroup.getMeasuredWidth());
            v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

            return new ButtonInfoHolder(v);

        }

        if (viewType == mLOCATION) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.frag_info_location, viewGroup, false);
            v.setMinimumWidth(viewGroup.getMeasuredWidth());
            v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

            return new LocationInfoHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (viewHolder.getItemViewType() == mBUTTON){
            Duration_Model mDurationModel = nodeList.get(position);
            final ButtonInfoHolder holder = (ButtonInfoHolder) viewHolder;
            ArrayList<Entry> yVals1 = setYAxis1();

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
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(50f);
            leftAxis.setTextColor(Color.rgb(255, 192, 56));

            YAxis rightAxis = holder.mChart.getAxisRight();
            rightAxis.setEnabled(false);


            set1 = new LineDataSet(yVals1, mDurationModel.getNodeId());
            set1.setFillAlpha(110);
            set1.setColor(Color.BLUE);
            set1.setCircleColor(Color.GREEN);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);


            // no description text
            holder.mChart.getDescription().setEnabled(false);

            // enable touch gestures
            holder.mChart.setTouchEnabled(true);
            holder.mChart.setDragDecelerationFrictionCoef(0.9f);

            // enable scaling and dragging
            holder.mChart.setDragEnabled(true);
            holder.mChart.setScaleEnabled(true);
            holder.mChart.setDrawGridBackground(false);
            holder.mChart.setHighlightPerDragEnabled(true);

            // if disabled, scaling can be done on x- and y-axis separately
            holder.mChart.setPinchZoom(true);

            XAxis mXAxis = holder.mChart.getXAxis();
            mXAxis.setTextSize(11f);
            mXAxis.setTextColor(Color.BLACK);
            mXAxis.setDrawGridLines(false);
            mXAxis.setDrawAxisLine(false);



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
            List<Address> list;
            try {
                list = geocoder.getFromLocation(mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), 1);
                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    loc = address.getLocality();


                }
            } catch (IOException e) {
                Log.e("DEBUG", "LOCATION ERR:" + e.getMessage());
            }
            holder.location.setText("Home : "+loc +" | "+String.valueOf((Double) mLat)+" : "+String.valueOf((Double) mLong));


        }

    }

    private ArrayList<String> setXAxisValues(){
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("10");
        xVals.add("20");
        xVals.add("30");
        xVals.add("30.5");
        xVals.add("40");

        return xVals;
    }


    private ArrayList<Entry> setYAxis1() {
        ArrayList<Entry> yVals1 = null;
        for (int i = 0; i < nodeList.size(); i++) {
            Duration_Model mDurationModel = nodeList.get(i);
            Log.d("DEBUG", "setYAxis1: " + mDurationModel.getDuration());




            yVals1 = new ArrayList<Entry>();
            yVals1.add(new Entry(mDurationModel.getDuration(), i));
        }

        return yVals1;
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

        }
    }

}
