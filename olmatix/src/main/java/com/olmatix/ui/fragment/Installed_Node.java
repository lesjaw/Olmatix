package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.olmatix.adapter.NodeAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Installed_NodeModel;
import com.olmatix.ui.activity.Detail_NodeActivity;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Installed_Node extends Fragment implements  OnStartDragListener {

    private View mView;
    private List<Installed_NodeModel> nodeList = new ArrayList<>();
    private RecyclerView mRecycleView;
    private FloatingActionButton mFab;
    private AlertDialog.Builder alertDialog;
    private View view;
    private Timer autoUpdate;
    private NodeAdapter adapter;
    private TextView etTopic,version;
    ImageView icon_node;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<Installed_NodeModel> data;
    private Paint p = new Paint();
    private ItemTouchHelper mItemTouchHelper;
    public static dbNodeRepo dbNodeRepo;
    private Installed_NodeModel installedNodeModel;
    private String inputResult;
    int flagReceiver=0;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String nice_name;
    String fwName;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_installed_node, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        data = new ArrayList<>();
        dbNodeRepo = new dbNodeRepo(getActivity());
        installedNodeModel = new Installed_NodeModel();
        initDialog();
        setupView();
        onClickListener();
        refreshHeader();
        //doSubAll();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                fwName = data.get(position).getFwName();
                nice_name = data.get(position).getNice_name_n();


                Intent i= new Intent(getActivity(), Detail_NodeActivity.class);
                i.putExtra("node_id",data.get(position).getNodesID());
                i.putExtra("node_name",fwName);
                i.putExtra("nice_name",nice_name);

                startActivity(i);

                /*ImageView picture=(ImageView)view.findViewById(R.id.state_conn);
                picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Single Click on Image :"+position,
                                Toast.LENGTH_SHORT).show();
                    }
                });*/
            }

            @Override
            public void onLongClick(View view, final int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Reset this Node?");
                builder.setMessage(data.get(position).getNice_name_n());

                builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nodeid = data.get(position).getNodesID();
                        String statusnode = data.get(position).getOnline();
                        if (statusnode.equals("true")) {

                            if (Connection.getClient().isConnected()) {
                                String topic = "devices/" + nodeid + "/$reset";
                                String payload = "true";
                                byte[] encodedPayload = new byte[0];
                                try {
                                    encodedPayload = payload.getBytes("UTF-8");
                                    MqttMessage message = new MqttMessage(encodedPayload);
                                    message.setQos(1);
                                    message.setRetained(true);
                                    Connection.getClient().publish(topic, message);

                                } catch (UnsupportedEncodingException | MqttException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getActivity(), "You don't connect to the server", Toast.LENGTH_LONG).show();
                                setRefresh();
                            }
                            Toast.makeText(getActivity(), "Successfully Reset", Toast.LENGTH_LONG).show();
                            //setRefresh();
                        } else {Toast.makeText(getActivity(), "Your device Offline, No reset have been done", Toast.LENGTH_LONG).show();}
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
        }));


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
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String mChange = intent.getStringExtra("NotifyChangeNode");

            if (mChange==null){
                mChange ="0";
            }
            if (mChange.equals("1")){
                updatelist();
                //Log.d("receiver", "NotifyAdd : " + mChange);
            }else if (mChange.equals("2")) {
                if (adapter != null)
                    updatelist();
                Log.d("receiver", "NotifyChangeNode : " + mChange);

            }
        }
    };


    private void updatelist (){
        adapter.notifyDataSetChanged();
        data.clear();
        data.addAll(dbNodeRepo.getNodeList());

        //adapter = new NodeAdapter(dbNodeRepo.getNodeList(),this);
        //mRecycleView.setAdapter(adapter);
        if(adapter != null)
        {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());

        }
        assert adapter != null;
        //adapter.setClickListener(this);

    }

    private void doSubAll(){

    int countDB = dbNodeRepo.getNodeList().size();
        Log.d("DEBUG", "Count list: "+countDB);

        for (int i = 0; i < countDB; i++) {
            final String mNodeID = data.get(i).getNodesID();
            Log.d("DEBUG", "Count list: "+mNodeID);
            String topic = "devices/" + mNodeID + "/#";
            int qos = 2;
            try {
                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("Subscribe", " device = " + mNodeID);
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

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (flagReceiver==0) {
            /*Intent i = new Intent(getActivity(), OlmatixService.class);
            getActivity().startService(i);*/

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatus"));

            Log.d("Receiver ", "Installed_Node = Starting..");
            flagReceiver = 1;
        }
        super.onResume();
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
                                inputResult =mEditText.getText().toString();
                                String topic = "devices/" + inputResult + "/$online";
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

        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);

        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        data.clear();
        data.addAll(dbNodeRepo.getNodeList());
        adapter = new NodeAdapter(data,this);
        mRecycleView.setAdapter(adapter);


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

        //adapter.setClickListener(this);

    }

    private void setRefresh() {

        data.clear();
        data.addAll(dbNodeRepo.getNodeList());

        adapter = new NodeAdapter(data,this);
        mRecycleView.setAdapter(adapter);
        //adapter.setClickListener(this);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initDialog(){
        alertDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater myLayout = LayoutInflater.from(getActivity());
        view = myLayout.inflate(R.layout.dialog_layout,null);
        alertDialog.setView(view);
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                adapter.notifyDataSetChanged();
                dialog.dismiss();

            }
        });
        etTopic = (TextView) view.findViewById(R.id.et_topic);
        version = (TextView) view.findViewById(R.id.version);
        icon_node = (ImageView) view.findViewById(R.id.icon_node);
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
                            Toast.makeText(getActivity(),"Successfully Inserted",Toast.LENGTH_LONG).show();
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
                    input.setText(data.get(position).getNice_name_n());

                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nice_name = input.getText().toString();
                            installedNodeModel.setNodesID(data.get(position).getNodesID());
                            installedNodeModel.setNice_name_n(nice_name);
                            dbNodeRepo.updateNameNice(installedNodeModel);
                            Toast.makeText(getActivity(),"Successfully Inserted",Toast.LENGTH_LONG).show();
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
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

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

    /*@Override
    public void itemClicked(View view, int position) {

    }*/

    public static interface ClickListener{
        public void onClick(View view,int position);
        public void onLongClick(View view,int position);
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
