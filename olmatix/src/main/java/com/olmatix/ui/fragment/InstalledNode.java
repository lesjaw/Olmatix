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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.olmatix.adapter.NodeAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;
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
    Boolean stateMqtt=false;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String nice_name;
    String fwName;
    Context installed_node;
    private ProgressDialog nDialog;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    private static String TAG = InstalledNode.class.getSimpleName();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_installed_node, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        installed_node=getContext();

        data = new ArrayList<>();
        dbNodeRepo = new dbNodeRepo(getActivity());
        installedNodeModel = new InstalledNodeModel();
        setupView();
        onClickListener();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                    fwName = data.get(position).getFwName();
                    nice_name = data.get(position).getNice_name_n();

                    String state = data.get(position).getOnline();
                    if (state.equals("true")) {

                        Intent i = new Intent(getActivity(), DetailNode.class);
                        i.putExtra("node_id", data.get(position).getNodesID());
                        i.putExtra("node_name", fwName);
                        i.putExtra("nice_name", nice_name);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),
                                nice_name + " is OFFLINE!, please check it, if the " + nice_name +
                                        " led blink something is wrong, slow blink mean no WiFi, fast blink mean no Internet",Snackbar.LENGTH_LONG).show();
                    }
            }
            @Override
            public void onLongClick(View view, final int position) {
/*
                ImageView imgNode;
                imgNode=(ImageView)view.findViewById(R.id.icon_node);
                imgNode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Reset this Node?");
                        builder.setMessage(data.get(position).getNice_name_n());
                        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nodeid = data.get(position).getNodesID();
                                String statusnode = data.get(position).getOnline();
                                if (statusnode.equals("true")) {
                                    sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                                    Log.d(TAG, "onClick: "+mStatusServer);
                                    if (mStatusServer) {
                                        String topic = "devices/" + nodeid + "/$reset";
                                        String payload = "true";
                                        byte[] encodedPayload = new byte[0];
                                        try {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);

                                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),
                                                    "Nodes succesfully reset",Snackbar.LENGTH_LONG).show();

                                        } catch (UnsupportedEncodingException | MqttException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),
                                                "You don't connect to the server",Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),
                                            "Your device/Node Offline",Snackbar.LENGTH_LONG).show();
                                }
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
                });
*/

            }
        }));
    }

    class load extends AsyncTask<Void, Integer, String> {


        protected void onPreExecute (){
            nDialog = new ProgressDialog(getContext());
            nDialog.setMessage("Loading Nodes, Please wait..");
            nDialog.setIndeterminate(true);
            nDialog.setCancelable(false);
            nDialog.show();        }

        protected String doInBackground(Void...arg0) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mStatusServer = sharedPref.getBoolean("conStatus", false);
            if (mStatusServer) {
                final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
                if (!mSwitch_conn) {
                    int countDB = dbNodeRepo.getNodeList().size();
                    data.addAll(dbNodeRepo.getNodeList());
                    for (int i = 0; i < countDB; i++) {
                        final String mNodeID = data.get(i).getNodesID();
                        for (int a = 0; a < 4; a++) {
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
                                topic = "devices/" + mNodeID + "/$localip";
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
            nDialog.dismiss();
            setAdapter();
            mFab.show();
        }
    }

    private void load(){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                nDialog = new ProgressDialog(getContext());
                nDialog.setMessage("Loading Nodes, Please wait..");
                nDialog.setIndeterminate(true);
                nDialog.setCancelable(false);
                nDialog.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (mStatusServer) {
                        final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
                        if (!mSwitch_conn) {
                            int countDB = dbNodeRepo.getNodeList().size();
                            data.addAll(dbNodeRepo.getNodeList());
                            for (int i = 0; i < countDB; i++) {
                                final String mNodeID = data.get(i).getNodesID();
                                for (int a = 0; a < 4; a++) {
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
                                        topic = "devices/" + mNodeID + "/$localip";
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                    nDialog.dismiss();
                    setAdapter();
                mFab.show();
            }

        };
        task.execute((Void[])null);
    }

    private void setAdapter(){
        data.clear();
        data.addAll(dbNodeRepo.getNodeList());
        adapter = new NodeAdapter(data,installed_node,this);
        mRecycleView.setAdapter(adapter);

    }
    private void refreshHeader() {
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
        }, 100, 5000); // updates GUI each 40 secs
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
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
         if (autoUpdate != null){
             autoUpdate.cancel();
         }
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
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
        data.clear();
        data.addAll(dbNodeRepo.getNodeList());

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

    private View.OnClickListener mFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText mEditText = new EditText(getContext());
                new AlertDialog.Builder(getContext())
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
        };
    }

    private void setupView() {
        mRecycleView    = (RecyclerView) mView.findViewById(R.id.rv);
        mSwipeRefreshLayout = (SwipeRefreshLayout)mView. findViewById(R.id.swipeRefreshLayout);

        mFab            = (FloatingActionButton) mView.findViewById(fab);

        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        new load().execute();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();
            }
        });

        initSwipe();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);


        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && mFab.isShown())
                {
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

    private void setRefresh() {
        load();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),"Node deleted",Snackbar.LENGTH_LONG).show();
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
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(),"Renaming Node success",Snackbar.LENGTH_LONG).show();
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
        //mItemTouchHelper.startDrag(viewHolder);
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
