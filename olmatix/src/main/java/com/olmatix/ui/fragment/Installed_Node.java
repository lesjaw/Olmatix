package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.app.Activity;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.olmatix.adapter.OlmatixAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.NodeModel;
import com.olmatix.service.OlmatixService;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Installed_Node extends Fragment implements OnStartDragListener {

    private View mView;
    private RecyclerView mRecycleView;
    private FloatingActionButton mFab;
    private AlertDialog.Builder alertDialog;
    private View view;
    private static OlmatixAdapter adapter;
    private TextView etTopic,version;
    ImageView icon_node;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<NodeModel> data;
    private Paint p = new Paint();
    private ItemTouchHelper mItemTouchHelper;
    HashMap<String,String> messageReceive = new HashMap<>();
    public static dbNodeRepo dbNodeRepo;
    private  NodeModel nodeModel;

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
        nodeModel = new NodeModel();
        initDialog();
        setupView();
        onClickListener();
    }

    private void onClickListener() {
        mFab.setOnClickListener(mFabClickListener());
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String device = intent.getStringExtra("device");
            String message = intent.getStringExtra("message");

            device = device.substring(device.indexOf("$")+1,device.length());
            messageReceive.put(device,message);

            saveandpersist();
        }

    };

    private void saveandpersist() {


        if(messageReceive.containsKey("online") && messageReceive.containsKey("nodes") && messageReceive.containsKey("name")
                && messageReceive.containsKey("localip") && messageReceive.containsKey("fwname") && messageReceive.containsKey("fwversion")
                && messageReceive.containsKey("signal") && messageReceive.containsKey("uptime") && messageReceive.containsKey("reset")
                && messageReceive.containsKey("ota"))
        {

                nodeModel.setNid(messageReceive.get("NodeId"));
                nodeModel.setOnline(messageReceive.get("online"));
                nodeModel.setNodes(messageReceive.get("nodes"));
                nodeModel.setName(messageReceive.get("name"));
                nodeModel.setLocalip(messageReceive.get("localip"));
                nodeModel.setFwName(messageReceive.get("fwname"));
                nodeModel.setFwVersion(messageReceive.get("fwversion"));
                nodeModel.setSignal(messageReceive.get("signal"));
                nodeModel.setUptime(messageReceive.get("uptime"));
                nodeModel.setReset(messageReceive.get("reset"));
                nodeModel.setOta(messageReceive.get("ota"));

                dbNodeRepo.insertDb(nodeModel);
                adapter.notifyDataSetChanged();
                messageReceive.clear();

        }
       /* if(messageReceive.containsKey("uptime") && ! messageReceive.containsKey("name"))
        {
            messageReceive.clear();
        }*/
    }


    @Override
    public void onStart() {
        Intent i = new Intent(getActivity(), OlmatixService.class);
        getActivity().startService(i);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event1"));
        super.onStart();
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
                                final String inputResult =mEditText.getText().toString();
                                String topic = "devices/" + inputResult + "/#";
                               // String topic = "devices/" + inputResult + "/$online";


                                int qos = 1;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                                    subToken.setActionCallback(new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                            messageReceive.put("NodeId",inputResult);

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
        mFab            = (FloatingActionButton) mView.findViewById(R.id.fab);

        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(mView.getContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        data.addAll(dbNodeRepo.getNodeList());

        adapter = new OlmatixAdapter(dbNodeRepo.getNodeList());
        mRecycleView.setAdapter(adapter);

        initSwipe();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);



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
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    adapter.removeItem(position);
                } else {
                    //removeView();
                    etTopic.setText(data.get(position).getName());
                    version.setText(data.get(position).getFwVersion());
                    icon_node.setImageResource(R.drawable.olmatixlogo);
                    alertDialog.show();

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

}
