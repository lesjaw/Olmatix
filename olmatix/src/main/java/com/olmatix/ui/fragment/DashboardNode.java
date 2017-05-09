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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.adapter.InfoAdapter;
import com.olmatix.adapter.NodeDashboardAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.SpinnerObject;
import com.olmatix.model.SpinnerObjectDash;
import com.olmatix.utils.GridSpacesItemDecoration;
import com.olmatix.utils.OlmatixUtils;
import com.olmatix.utils.SpinnerListener;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.adapter.InfoAdapter.mLOCATION;


public class DashboardNode extends Fragment implements OnStartDragListener {

    private View mView;
    private RecyclerView mRecycleView;
    private RecyclerView mRecycleViewInfo;
    private FloatingActionButton mFab;
    NodeDashboardAdapter adapter;
    InfoAdapter infoAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DashboardNodeModel dashboardNodeModel;
    public  static dbNodeRepo mDbNodeRepo;
    private static ArrayList<DashboardNodeModel> data;
    Spinner mSpinner;
    private int mDatasetTypes[] = {mLOCATION}; //view types
    Context dashboardnode;
    private ItemTouchHelper mItemTouchHelper;
    private String Distance;
    private String dist;
    CoordinatorLayout coordinatorLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_dashboard, container, false);
        coordinatorLayout=(CoordinatorLayout)mView.findViewById(R.id.main_content);

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

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);

        setupView();
        onClickListener();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),mRecycleView, new ClickListener() {

            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                adapter.removeItem(position);
                TSnackbar snackbar = TSnackbar.make((coordinatorLayout),"Dashboard item deleted"
                        ,TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.show();
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
                onTouchListener(0);
                mSpinner = new Spinner(getActivity());
                List<SpinnerObjectDash> lables = mDbNodeRepo.getAllLabelsDash();
                ArrayAdapter<SpinnerObjectDash> dataAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item,lables);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Add Node")
                        .setMessage("Please choose your existing Nodes!")
                        .setView(mSpinner)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSpinner.setOnItemSelectedListener(new SpinnerListener());
                                int databaseId = Integer.parseInt (String.valueOf(( (SpinnerObjectDash) mSpinner.getSelectedItem ()).getId()));                                Log.d("DEBUG", "onClick: "+String.valueOf(databaseId));
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

    private View.OnTouchListener mFabTouchListener(){
        return  new View.OnTouchListener() {
            float dX;
            float dY;
            int lastAction;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        view.setY(event.getRawY() + dY);
                        view.setX(event.getRawX() + dX);
                        lastAction = MotionEvent.ACTION_MOVE;
                        break;

                    case MotionEvent.ACTION_UP:
                        mSpinner = new Spinner(getActivity());
                        List<SpinnerObject> lables = mDbNodeRepo.getAllLabels();
                        ArrayAdapter<SpinnerObject> dataAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item,lables);

                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinner.setAdapter(dataAdapter);
                        if (lastAction == MotionEvent.ACTION_DOWN)
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Add Node")
                                .setMessage("Please choose your existing Nodes!")
                                .setView(mSpinner)
                                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSpinner.setOnItemSelectedListener(new SpinnerListener());
                                        int databaseId = Integer.parseInt (String.valueOf(( (SpinnerObject) mSpinner.getSelectedItem () ).getDatabaseId ()));
                                        dashboardNodeModel.setNice_name_d(String.valueOf(databaseId));
                                        mDbNodeRepo.insertFavNode(dashboardNodeModel);
                                        setRefresh();
                                    }
                                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
                        onTouchListener(0);

                        break;
                    case MotionEvent.ACTION_BUTTON_PRESS:

                    default:
                        return false;
                }
                return true;
            }
        };

    }

    private void onTouchListener(int drag) {
        if (drag==1) {
            mFab.setOnTouchListener(mFabTouchListener());
        } else {
            mFab.setOnTouchListener(null);
        }
    }

    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mRecycleViewInfo    = (RecyclerView) mView.findViewById(R.id.rv1);
        View mViewDash1 = mView.findViewById(R.id.view_dash);
        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);

        mRecycleView.setHasFixedSize(true);
        mRecycleViewInfo.setHasFixedSize(true);

        int mNoOfColumns = OlmatixUtils.calculateNoOfColumns(getActivity());

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);

        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.addItemDecoration(new GridSpacesItemDecoration(OlmatixUtils.dpToPx(2),true));

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecycleViewInfo.setLayoutManager(horizontalLayoutManagaer);

        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewInfo.setItemAnimator(new DefaultItemAnimator());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                data.addAll(mDbNodeRepo.getNodeDetailDash());
            }
        });


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
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
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

        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    onTouchListener(1);
                return false;
            }
        });

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
        onTouchListener(0);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFab.hide();

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
            }
            updatelist();
            if (!String.valueOf(DistService).trim().equals("null")){
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
        mItemTouchHelper.startDrag(viewHolder);

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private DashboardNode.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        RecyclerTouchListener(Context context, final RecyclerView recycleView, final DashboardNode.ClickListener clicklistener){

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
