package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.olmatix.adapter.InfoAdapter;
import com.olmatix.adapter.NodeDashboardAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.SpinnerObject;
import com.olmatix.utils.GridSpacesItemDecoration;
import com.olmatix.utils.OlmatixUtils;
import com.olmatix.utils.SpinnerListener;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.adapter.InfoAdapter.mBUTTON;
import static com.olmatix.adapter.InfoAdapter.mLOCATION;


public class DashboardNode extends Fragment implements
        OnStartDragListener{

    private View mView;
    private RecyclerView mRecycleView;
    private RecyclerView mRecycleViewInfo;
    private FloatingActionButton mFab;
    NodeDashboardAdapter adapter;
    InfoAdapter infoAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemTouchHelper mItemTouchHelper;
    private DashboardNodeModel dashboardNodeModel;
    public  static dbNodeRepo mDbNodeRepo;
    private Paint p = new Paint();
    private static ArrayList<DashboardNodeModel> data;
    Spinner mSpinner;
    private int mDatasetTypes[] = {mLOCATION, mBUTTON}; //view types
    Context dashboardnode;
    private LocationManager locationManager;

    private String mProvider;
    private LocationManager mLocateMgr;
    private Location mLocation;
    //private Context mContext;
    private String Distance;
    private String dist;
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

        mDbNodeRepo = new dbNodeRepo(getActivity());
        dashboardNodeModel= new DashboardNodeModel();
        dashboardnode=getActivity();
        mDbNodeRepo.getAllScene();
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

                List<SpinnerObject> lables = mDbNodeRepo.getAllLabels();

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
                                mDbNodeRepo.insertFavNode(dashboardNodeModel);
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
        View mViewDash1 = mView.findViewById(R.id.view_dash);
        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);

        mRecycleView.setHasFixedSize(true);
        mRecycleViewInfo.setHasFixedSize(true);

        int mNoOfColumns = OlmatixUtils.calculateNoOfColumns(getContext());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mNoOfColumns);

        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.addItemDecoration(new GridSpacesItemDecoration(OlmatixUtils.dpToPx(2),true));

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecycleViewInfo.setLayoutManager(horizontalLayoutManagaer);

        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewInfo.setItemAnimator(new DefaultItemAnimator());

        data.clear();
        data.addAll(mDbNodeRepo.getNodeDetailDash());
        adapter = new NodeDashboardAdapter(data,dashboardnode,this);
        mRecycleView.setAdapter(adapter);

        dist = "";
        dist = Distance;
        infoAdapter = new InfoAdapter(dist, mDatasetTypes,dashboardnode, this);
        mRecycleViewInfo.setAdapter(infoAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();

            }
        });


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRecycleViewInfo.setVisibility(View.GONE);
            mViewDash1.setVisibility(View.GONE);

            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            p.addRule(RelativeLayout.BELOW, R.id.view_dash1);
            mRecycleView.setLayoutParams(p);

        }
        else {
            mRecycleViewInfo.setVisibility(View.VISIBLE);

        }

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
        data.addAll(mDbNodeRepo.getNodeDetailDash());
        adapter = new NodeDashboardAdapter(data,dashboardnode,this);
        mRecycleView.setAdapter(adapter);

        dist="";
        dist = Distance;
        infoAdapter = new InfoAdapter(dist, mDatasetTypes,dashboardnode, this);
        mRecycleViewInfo.setAdapter(infoAdapter);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("NotifyChangeDetail");
            String DistService = intent.getStringExtra("distance");
            if (message==null){
                message = "1";
            }
            if (message.equals("2")){
                updatelist();
                //Log.d("receiver", "Notifydashboard : " + message);
            }
            updatelist();
            if (!String.valueOf(DistService).trim().equals(null)){
                Distance = DistService;
                resetAdapter();

            } else {
                Distance = "Unknown";
                resetAdapter();

            }

        }
    };

    private void updatelist (){

        adapter.notifyDataSetChanged();
        data.clear();
        data.addAll(mDbNodeRepo.getNodeDetailDash());
        if(adapter != null){
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
        //Log.d("Receiver ", "Dashboard = Starting..");
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
        //mItemTouchHelper.startDrag(viewHolder);

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private DashboardNode.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final DashboardNode.ClickListener clicklistener){

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


    public void resetAdapter(){

        infoAdapter.notifyDistance(Distance);

    }

}
