package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.olmatix.adapter.InfoAdapter;
import com.olmatix.adapter.NodeDashboardAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Dashboard_NodeModel;
import com.olmatix.model.Installed_NodeModel;
import com.olmatix.model.SpinnerObject;
import com.olmatix.ui.activity.MainActivity;
import com.olmatix.utils.GridAutofitLayoutManager;
import com.olmatix.utils.GridSpacingItemDecoration;
import com.olmatix.utils.OlmatixUtils;
import com.olmatix.utils.SpinnerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.olmatix.adapter.InfoAdapter.mBUTTON;
import static com.olmatix.adapter.InfoAdapter.mLOCATION;


public class Dashboard_Node extends Fragment implements
        OnStartDragListener,
        LocationListener {

    private View mView;
    private RecyclerView mRecycleView;
    private RecyclerView mRecycleViewInfo;
    private FloatingActionButton mFab;
    NodeDashboardAdapter adapter;
    private InfoAdapter infoAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemTouchHelper mItemTouchHelper;
    private Dashboard_NodeModel dashboardNodeModel;
    private Installed_NodeModel installedNodeModel;
    public  static dbNodeRepo dbNodeRepo;
    private Paint p = new Paint();
    private static ArrayList<Dashboard_NodeModel> data;
    Spinner mSpinner;
    private int mDatasetTypes[] = {mLOCATION, mBUTTON}; //view types
    Context dashboardnode;
    private LocationManager locationManager;

    private String mProvider;
    private LocationManager mLocateMgr;
    private Location mLocation;
    //private Context mContext;
    private String Distance;
    String adString = "";
    String loc = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_dashboard, container, false);
        return mView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        data = new ArrayList<>();

        dbNodeRepo = new dbNodeRepo(getActivity());
        dashboardNodeModel= new Dashboard_NodeModel();

        dashboardnode=getActivity();


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        setupView();
        onClickListener();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),mRecycleView, new ClickListener() {

            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                adapter.removeItem(position);
                Toast.makeText(getActivity(),"Successfully Deleted",Toast.LENGTH_LONG).show();
                setRefresh();
            }
        }));
    }

    private void onClickListener() {
        mFab.setOnClickListener(mFabClickListener());
    }

    private View.OnClickListener mFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner = new Spinner(getContext());
                String[] labelData;

                List<SpinnerObject> lables = dbNodeRepo.getAllLabels();

                ArrayAdapter<SpinnerObject> dataAdapter = new ArrayAdapter<SpinnerObject>(getActivity(),
                        android.R.layout.simple_spinner_item,lables);
                dataAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);

                new AlertDialog.Builder(getContext())
                        .setTitle("Add Node")
                        .setMessage("Please choose your existing Nodes!")
                        .setView(mSpinner)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSpinner.setOnItemSelectedListener(new SpinnerListener());

                                //Log.d("DEBUG", "onItemSelected: "+ mSpinner.getSelectedItem().toString());
                                int databaseId = Integer.parseInt (String.valueOf(( (SpinnerObject) mSpinner.getSelectedItem () ).getId ()));
                                System.out.println(String.valueOf(databaseId));

                                dashboardNodeModel.setNice_name_d(String.valueOf(databaseId));
                                dbNodeRepo.insertFavNode(dashboardNodeModel);
                                setRefresh();

                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

            }
        };
    }

    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mRecycleViewInfo    = (RecyclerView) mView.findViewById(R.id.rv1);

        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);

        mRecycleView.setHasFixedSize(true);
        mRecycleViewInfo.setHasFixedSize(true);

        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(getActivity(), 200 );
        mRecycleView.setLayoutManager(layoutManager);

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecycleViewInfo.setLayoutManager(horizontalLayoutManagaer);

        int mNoOfColumns = GridAutofitLayoutManager.DEFAULT_SPAN_COUNT;
        int spacing = 10;
        boolean includeEdge = true;
        mRecycleView.addItemDecoration(new GridSpacingItemDecoration(mNoOfColumns, spacing, includeEdge));

        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewInfo.setItemAnimator(new DefaultItemAnimator());

        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailDash());
        adapter = new NodeDashboardAdapter(data,dashboardnode,this);
        mRecycleView.setAdapter(adapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();

            }
        });

        infoAdapter = new InfoAdapter( Distance, mDatasetTypes,dashboardnode, this);
        mRecycleViewInfo.setAdapter(infoAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 ||dy<0 && mFab.isShown())
                {
                    mFab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    mFab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void setRefresh() {
        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailDash());
        adapter = new NodeDashboardAdapter(data,dashboardnode,this);
        mRecycleView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }



    @Override
    public void onStart() {
        super.onStart();
        initLocationProvider();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("NotifyChangeDetail");
            String msg = intent.getStringExtra("MQTT State");
            if (message==null){
                message = "1";
            }
            if (message.equals("2")){
                updatelist();
                Log.d("receiver", "Notifydashboard : " + message);
            }
            updatelist();

        }
    };

    private void updatelist (){

        adapter.notifyDataSetChanged();
        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailDash());
        if(adapter != null){
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
        //setRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
        Log.d("Receiver ", "Dashboard = Starting..");
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private Dashboard_Node.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final Dashboard_Node.ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }



        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void initLocationProvider() {


        PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());

        mProvider = locationManager.getBestProvider(OlmatixUtils.getGeoCriteria(), true);

        boolean enabled = (mProvider != null && locationManager.isProviderEnabled(mProvider) &&mPrefHelper.getHomeLatitude() != 0);
        if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)) {
            if (enabled) {
                locationManager.requestLocationUpdates(mProvider, OlmatixUtils.POSITION_UPDATE_INTERVAL,
                        OlmatixUtils.POSITION_UPDATE_MIN_DIST, (LocationListener) this);
                Location location = locationManager.getLastKnownLocation(mProvider);
                // Initialize the location fields
                if (location != null) {
                    onLocationChanged(location);
                }
            } else if (mPrefHelper.getHomeLatitude() != 0) {

            } else {

            }
        } else//permesso mancante
        {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    OlmatixUtils.OLMATIX_PERMISSIONS_ACCESS_COARSE_LOCATION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case OlmatixUtils.OLMATIX_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mProvider = mLocateMgr.getBestProvider(OlmatixUtils.getGeoCriteria(), true);
                    Log.w("DEBUG", "MY_PERMISSIONS_ACCESS_COARSE_LOCATION permission granted");

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.wtf("DEBUG", "Need permission");
                        return;
                    }
                    mLocateMgr.requestLocationUpdates(mProvider, OlmatixUtils.POSITION_UPDATE_INTERVAL,
                            OlmatixUtils.POSITION_UPDATE_MIN_DIST, (LocationListener) this);
                    mLocation = mLocateMgr.getLastKnownLocation(mProvider);

                    Log.d("DEBUG", "LastKnown: "+mLocation);
                    // Initialize the location fields
                    if (mLocation != null) {
                        onLocationChanged(mLocation);
                    }

                }
                return;
            }
        }
    }

    public void onLocationChanged(Location mLocation) {
        final double lat = (mLocation.getLatitude());
        final double lng = (mLocation.getLongitude());

        if (lat!=0 && lng!=0) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    final Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

                    try {
                        List<Address> list;
                        list = geocoder.getFromLocation(lat, lng, 1);
                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            loc = address.getLocality();

                            if (address.getAddressLine(0) != null)
                                adString = ", " + address.getAddressLine(0);
                        }

                    } catch (final IOException e) {
                        ((MainActivity) dashboardnode).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("DEBUG", "Geocoder ERROR", e);
                            }
                        });
                        loc = OlmatixUtils.gpsDecimalFormat.format(lat) + " : " + OlmatixUtils.gpsDecimalFormat.format(lng);
                    }
                    Log.d("DEBUG", "Current Location : " + loc);

                    final float[] res = new float[3];
                    final PreferenceHelper mPrefHelper = new PreferenceHelper(dashboardnode);
                    Location.distanceBetween(lat, lng, mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), res);
                    if (mPrefHelper.getHomeLatitude() != 0) {
                        ((MainActivity) dashboardnode).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String unit = " m";
                                if (res[0] > 2000) {// usa chilometri
                                    unit = " km";
                                    res[0] = res[0] / 1000;
                                }
                                Log.d("DEBUG", "Distance: " + (int) res[0] + unit);
                                Distance = loc +", it's "+ (int) res[0] + unit ;
                                resetAdapter();
                            }
                        });
                    }
                }

            }).start();
        }
        Log.d("DEBUG", "Distance OnCreate: " + Distance);

    }

    public void resetAdapter(){
        infoAdapter = new InfoAdapter( Distance, mDatasetTypes,dashboardnode, this);
        mRecycleViewInfo.setAdapter(infoAdapter);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
