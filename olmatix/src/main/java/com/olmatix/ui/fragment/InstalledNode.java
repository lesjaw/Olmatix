package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.adapter.NodeAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.activity.CameraActivity;
import com.olmatix.ui.activity.GatewayActivity;
import com.olmatix.ui.activity.PhoneActivity;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.olmatix.lesjaw.olmatix.R.id.fab;


public class InstalledNode extends Fragment implements  OnStartDragListener {

    private View mView;
    private List<InstalledNodeModel> nodeList = new ArrayList<>();
    private RecyclerView mRecycleView;
    private FloatingActionButton mFab;
    private Timer autoUpdate;
    private NodeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<InstalledNodeModel> data;
    private Paint p = new Paint();
    private ItemTouchHelper mItemTouchHelper;
    public static dbNodeRepo dbNodeRepo;
    private InstalledNodeModel installedNodeModel;
    private String inputResult;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String nice_name;
    String fwName;
    Context installed_node;
    private ProgressDialog nDialog;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    private static String TAG = InstalledNode.class.getSimpleName();
    private CoordinatorLayout coordinatorLayout;
    boolean onrefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_installed_node, container, false);
        coordinatorLayout=(CoordinatorLayout)mView.findViewById(R.id.installednode);

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        installed_node=getActivity();

        data = new ArrayList<>();
        dbNodeRepo = new dbNodeRepo(getActivity());
        installedNodeModel = new InstalledNodeModel();
        setupView();
        onClickListener();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                ImageView picture=(ImageView)view.findViewById(R.id.rvbut);
                picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fwName = data.get(position).getFwName();
                        nice_name = data.get(position).getNice_name_n();

                        String camid = data.get(position).getFwName();
                        Log.d(TAG, "onClick: "+camid+" " +fwName);

                        if (camid.equals("smartcam")) {
                            Intent i = new Intent(getActivity(), CameraActivity.class);
                            i.putExtra("nodeid", data.get(position).getNodesID());
                            i.putExtra("nice_name", nice_name);
                            startActivity(i);

                        } else if (camid.equals("olmatixapp")) {
                            Intent i = new Intent(getActivity(), PhoneActivity.class);
                            i.putExtra("nodeid", data.get(position).getNodesID());
                            i.putExtra("nice_name", nice_name);
                            startActivity(i);

                        } else if (camid.equals("smartgateway")) {
                            String state = data.get(position).getOnline();
                            if (state.equals("true")) {
                                Intent i = new Intent(getActivity(), GatewayActivity.class);
                                i.putExtra("nodeid", data.get(position).getNodesID());
                                i.putExtra("nice_name", nice_name);
                                startActivity(i);
                            } else {
                                TSnackbar snackbar = TSnackbar.make((coordinatorLayout), nice_name + " is OFFLINE!, please check it, " +
                                        "if led blink something is wrong, slow blink mean no WiFi, " +
                                        "fast blink mean no Internet", TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.show();
                            }

                        } else {
                            String state = data.get(position).getOnline();
                            if (state.equals("true")) {

                                Intent i = new Intent(getActivity(), DetailNode.class);
                                i.putExtra("node_id", data.get(position).getNodesID());
                                i.putExtra("node_name", fwName);
                                i.putExtra("nice_name", nice_name);
                                startActivity(i);
                            } else {
                                TSnackbar snackbar = TSnackbar.make((coordinatorLayout), nice_name + " is OFFLINE!, please check it, " +
                                        "if led blink something is wrong, slow blink mean no WiFi, " +
                                        "fast blink mean no Internet", TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.show();
                            }
                        }
                    }
                });


            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));
    }

    private void onTouchListener(int drag) {
        if (drag==1) {
            mFab.setOnTouchListener(mFabTouchListener());
        } else {
            mFab.setOnTouchListener(null);
        }
    }

    private class load extends AsyncTask<Void, Integer, String> {

        View rootView;

        public load(CoordinatorLayout coordinatorLayout) {
            this.rootView = coordinatorLayout;
        }

        protected void onPreExecute (){
            onrefresh = true;

        }

        protected String doInBackground(Void...arg0) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mStatusServer = sharedPref.getBoolean("conStatus", false);
            Log.d(TAG, "doInBackground: "+mStatusServer);
            if (mStatusServer) {
                final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
                if (!mSwitch_conn) {
                    Log.d(TAG, "doInBackground: "+mSwitch_conn);
                    int countDB = dbNodeRepo.getNodeList().size();
                    data.addAll(dbNodeRepo.getNodeList());
                    for (int i = 0; i < countDB; i++) {
                        final String mNodeID = data.get(i).getNodesID();
                        for (int a = 0; a < 7; a++) {
                            String topic = "";
                            if (a == 0) {
                                topic = "devices/" + mNodeID + "/$online";
                            }
                            if (a == 1) {
                                topic = "devices/" + mNodeID + "/$signal";
                            }
                            if (a == 2) {
                                topic = "devices/" + mNodeID + "/$uptime";
                            }
                            if (a == 3) {
                                topic = "devices/" + mNodeID + "/$fwname";
                            }
                            if (a == 4) {
                                topic = "devices/" + mNodeID + "/$localip";
                            }
                            if (a == 5) {
                                topic = "devices/" + mNodeID + "/$location";
                            }
                            if (a == 6) {
                                topic = "devices/" + mNodeID + "/$calling";
                            }
                            int qos = 2;
                            try {
                                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    }
                                });
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    data.clear();
                }
            }

            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer...a){
//            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            setAdapter();
            mFab.show();
            //mSwipeRefreshLayout.setRefreshing(false);
            TSnackbar snackbar = TSnackbar.make(rootView, " Refreshing is half done, continue to each node check..", TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.show();
        }
    }

    private class getOnline extends AsyncTask<Void, Integer, String> {
        View rootView;
        int i;
        public getOnline(CoordinatorLayout coordinatorLayout) {
            this.rootView = coordinatorLayout;
        }

        protected void onPreExecute (){
            onrefresh = true;
        }

        protected String doInBackground(Void...arg0) {
            Log.d(TAG, "doing http GET: ");
            int countDB = dbNodeRepo.getNodeList().size();
            data.addAll(dbNodeRepo.getNodeList());
            for (i = 0; i < countDB; i++) {

                try {
                    Thread.currentThread();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String id_device = data.get(i).getNodesID();

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                final String urlGet = "http://cloud.olmatix.com:1880/API/GET/DEVICE?id=" + id_device;
                Log.d(TAG, "doInBackground: "+urlGet);

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, urlGet,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.d(TAG, "onResponse: " + id_device + " response "+  response);
                                String state;

                                String result = response.trim();
                                if (result.equals("false")|| Objects.equals(result, "")){
                                    //Log.d("DEBUG", "FALSE: "+response);
                                    state = "OFFLINE";

                                } else {
                                    state = "ONLINE";
                                    installedNodeModel.setOnline("true");
                                    installedNodeModel.setNodesID(id_device);
                                    dbNodeRepo.updateOnline(installedNodeModel);
                                    updatelist();
                                }
                                //Toast.makeText(getActivity(),"Refreshing Node "+id_device +" is done, and it's "+state +
                                        //", checking next node..",Toast.LENGTH_LONG).show();

                                TSnackbar snackbar = TSnackbar.make(rootView, id_device+" is "+state+", refreshing now & continue to " +
                                        "node no "+i+" from "+countDB+", "+(countDB-i)+" node left", TSnackbar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }

            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer...a){
//            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            setAdapter();
            mFab.show();
            TSnackbar snackbar = TSnackbar.make(rootView, "We are almost done refreshing, one node left..", TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.show();
            mSwipeRefreshLayout.setRefreshing(false);
            onrefresh = false;
        }
    }

    private void refreshnode(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        Log.d(TAG, "doInBackground: "+mStatusServer);
        if (mStatusServer) {
            final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
            if (!mSwitch_conn) {
                Log.d(TAG, "doInBackground: "+mSwitch_conn);
                int countDB = dbNodeRepo.getNodeList().size();
                data.addAll(dbNodeRepo.getNodeList());
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID = data.get(i).getNodesID();
                    for (int a = 0; a < 5; a++) {
                        String topic = "";
                        if (a == 0) {
                            topic = "devices/" + mNodeID + "/$online";
                        }
                        if (a == 1) {
                            topic = "devices/" + mNodeID + "/$signal";
                        }
                        if (a == 2) {
                            topic = "devices/" + mNodeID + "/$uptime";
                        }
                        if (a == 3) {
                            topic = "devices/" + mNodeID + "/$fwname";
                        }
                        if (a == 4) {
                            topic = "devices/" + mNodeID + "/$localip";
                        }
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                            int finalA = a;
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d(TAG, "onSuccess: " + finalA +" "+mNodeID);
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
                data.clear();
            }
        }
        setAdapter();
        mFab.show();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setAdapter(){
        data.clear();
        data.addAll(dbNodeRepo.getNodeList());
        adapter = new NodeAdapter(data,installed_node,this);
        mRecycleView.setAdapter(adapter);

    }

    public void refreshHeader() {
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    }
                });
            }
        }, 100, 10000); // updates GUI each 40 secs
    }

    public void cancelSchedule(){
        Log.d(TAG, "cancelSchedule: "+autoUpdate);
        if (autoUpdate != null){
            autoUpdate.cancel();
        }

    }

    private void onClickListener() {
        mFab.setOnClickListener(mFabClickListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        mFab.hide();
        refreshHeader();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (autoUpdate != null) {
            autoUpdate.cancel();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
         if (autoUpdate != null){
             autoUpdate.cancel();
         }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mChange = intent.getStringExtra("NotifyChangeNode");
            //Log.d(TAG, "onReceive: ");
            if (mChange==null){
                mChange ="0";
            }
            if (mChange.equals("1")){
                updatelist();
            }else if (mChange.equals("2")) {
                if (adapter != null)
                    updatelist();
            }
        }
    };


    private void updatelist (){
        adapter.notifyDataSetChanged();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                data.addAll(dbNodeRepo.getNodeList());
            }
        });
        if(adapter != null) {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
        assert adapter != null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
        super.onResume();
    }

    private void sendMessage() {
        Intent intent = new Intent("addNode");
        intent.putExtra("NodeID", inputResult);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private View.OnTouchListener mFabTouchListener(){
        return  new View.OnTouchListener() {
            float dX;
            float dY;
            int lastAction;
            float distanceX;

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
                        distanceX = event.getRawX()-event.getRawX();
                        if (Math.abs(distanceX)< 10) {

                            final EditText mEditText = new EditText(getActivity());
                            if (lastAction == MotionEvent.ACTION_DOWN)
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Add Node")
                                        .setMessage("Please type Olmatix product ID!")
                                        .setView(mEditText)
                                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                inputResult = mEditText.getText().toString();
                                                sendMessage();


                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                }).show();
                        }

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

    private View.OnClickListener mFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTouchListener(0);
                final EditText mEditText = new EditText(getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle("Add Node")
                        .setMessage("Please type Olmatix product ID!")
                        .setView(mEditText)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String input = mEditText.getText().toString();
                                String[] outputDevices = input.split("-");
                                if (outputDevices[0].equals("cam")|| (outputDevices[0].equals("Cam"))){
                                    addCameNode(outputDevices[1]);

                                } else if (outputDevices[0].equals("OlmatixApp")) {
                                    dosubOlmatixApp(input);
                                    inputResult = input;
                                    sendMessage();
                                } else if (outputDevices[0].equals("gtw")||(outputDevices[0].equals("Gtw"))) {

                                    inputResult = input;
                                    sendMessage();
                                } else {

                                        inputResult = input;
                                        sendMessage();
                                    }


                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
            }
        };
    }

    private void dosubOlmatixApp(String input){

        for (int a = 0; a < 2; a++) {
            String topic = "";
            if (a==0) {
                topic = "devices/" + input + "/$calling";
            }
            if (a==1) {
                topic = "devices/" + input + "/$location";
            }
            int qos = 2;
            try {
                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                int finalA = a;
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "onSuccess: "+ finalA +" "+input);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        dosubOlmatixAppLoc(input);
    }

    private void dosubOlmatixAppLoc(String input){


        String topic = "devices/" + input + "/$location";

            int qos = 2;
            try {
                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Subscribe location of: "+input);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

    }

    private void addCameNode(String mCamid){
        installedNodeModel.setNodesID(mCamid);
        installedNodeModel.setOnline("true");
        installedNodeModel.setFwName("smartcam");
        installedNodeModel.setSignal("0");
        installedNodeModel.setLocalip("103.43.47.61");
        installedNodeModel.setUptime("0");
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        installedNodeModel.setAdding(now.getTimeInMillis());

        dbNodeRepo.insertDbCam(installedNodeModel);
    }

    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);
        mFab            = (FloatingActionButton) mView.findViewById(fab);
        mRecycleView.setHasFixedSize(true);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mFab.hide();
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mFab.show();
        }

        layoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        //initSwipe();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                data.addAll(dbNodeRepo.getNodeList());
            }
        });

        adapter = new NodeAdapter(data,installed_node,this);
        mRecycleView.setAdapter(adapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();
            }
        });

        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    onTouchListener(1);
                return false;
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && mFab.isShown()) {
                    mFab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFab.show();

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if ( menuVisible ) {

            //new load().execute();

        } else  {
            /**
             * Fragment not currently Visible.
             */
        }
    }

    private void setRefresh() {
        onTouchListener(0);
        if (!onrefresh) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout, " Refreshing Nodes now..", TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.show();
            new load(coordinatorLayout).execute();
            new getOnline(coordinatorLayout).execute();
        } else {
            Toast.makeText(getActivity(),"Refresh already running in background, " +
                    "it takes couple of minutes depending your installed nodes",Toast.LENGTH_SHORT).show();
        }
        //refreshnode();
        //setAdapter();
        mFab.show();
        //mSwipeRefreshLayout.setRefreshing(false);

    }

    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |
                ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete this Node?");
                    builder.setMessage(data.get(position).getNice_name_n());


                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.removeItem(position);
                            TSnackbar snackbar = TSnackbar.make((coordinatorLayout), nice_name + " Node deleted",TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                            setRefresh();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                } else {
                    //removeView();
                    adapter.notifyDataSetChanged();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Rename Node");
                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    if (data.get(position).getNice_name_n()!=null) {
                        input.setText(data.get(position).getNice_name_n());
                    } else{
                        input.setText(data.get(position).getFwName());
                    }
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nice_name = input.getText().toString();
                            installedNodeModel.setNodesID(data.get(position).getNodesID());
                            installedNodeModel.setNice_name_n(nice_name);
                            dbNodeRepo.updateNameNice(installedNodeModel);
                            TSnackbar snackbar = TSnackbar.make((coordinatorLayout), nice_name + " Renaming Node success",TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                            setRefresh();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();


                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecycleView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

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

}
