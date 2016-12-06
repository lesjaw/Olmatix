package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.olmatix.adapter.CustomAdapter;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.utils.Connection;
import com.olmatix.model.MyData;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DataModel;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;


import java.util.ArrayList;


public class Installed_Node extends Fragment  implements OnStartDragListener {

    private static CustomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;
    public static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;
    private String _Node_Name;
    private Paint p = new Paint();
    private ItemTouchHelper mItemTouchHelper;
    private AlertDialog.Builder alertDialog;
    private View view;
    private TextView etTopic,version;
    ImageView icon_node;


    public static Installed_Node newInstance() {
        Installed_Node fragment = new Installed_Node();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragInstalledNode = inflater.inflate(R.layout.frag_installed_node, container, false);

        FloatingActionButton fab = (FloatingActionButton) fragInstalledNode.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final EditText m_Text = new EditText(getContext());

                new AlertDialog.Builder(getContext())
                        .setTitle("Add Node")
                        .setMessage("Please type Olmatix product ID!")
                        .setView(m_Text)
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputResult = m_Text.getText().toString();

                                String topic = "devices/" + inputResult + "/$online";
                                int qos = 1;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                                    subToken.setActionCallback(new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                        }

                                        @Override
                                        public void onFailure(IMqttToken asyncActionToken,
                                                              Throwable exception) {
                                            // The subscription could not be performed, maybe the user was not
                                            // authorized to subscribe on the specified topic e.g. using wildcards
                                        }
                                    });
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }


                                /*_Node_Name = "test";
                                Intent intent = getActivity().getIntent();
                                _Node_Name = intent.getStringExtra(_Node_Name);
                                dbNodeRepo repo = new dbNodeRepo(getContext());
                                dbNode node = new dbNode();
                                repo.getNodeByNode(_Node_Name);
                                node.node=inputResult;

                                ArrayList<HashMap<String, String>> nodeList =  repo.getNodeList();
                                if(nodeList.size()!=0) {


                                    node.node=_Node_Name;
                                    if (_Node_Name != inputResult){
                                        _Node_Name = String.valueOf(repo.insert(node));
                                        Toast.makeText(getContext(),"New Node Insert " + _Node_Name,Toast.LENGTH_SHORT).show();

                                    }else{
                                        repo.update(node);
                                        Toast.makeText(getContext(),"You already have this Node",Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(getContext(), "No Node!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(),"Node Name : " + _Node_Name,Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(),"InputResult : " + inputResult,Toast.LENGTH_SHORT).show();
                                    if (_Node_Name != inputResult) {
                                        _Node_Name = String.valueOf(repo.insert(node));
                                        Toast.makeText(getContext(), "New Node Insert " + _Node_Name, Toast.LENGTH_SHORT).show();

                                    } else {
                                        repo.update(node);
                                        Toast.makeText(getContext(), "You already have this Node", Toast.LENGTH_SHORT).show();
                                    }
                                }*/

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();


            }
        });

        initDialog();
        recyclerView = (RecyclerView) fragInstalledNode.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(fragInstalledNode.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        data = new ArrayList<DataModel>();
        for (int i = 0; i < MyData.nameArray.length; i++) {
            data.add(new DataModel(
                    MyData.nameArray[i],
                    MyData.versionArray[i],
                    MyData.id_[i],
                    MyData.drawableArray[i]
            ));
        }

        removedItems = new ArrayList<Integer>();

        adapter = new CustomAdapter(data);
        recyclerView.setAdapter(adapter);

        initSwipe();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);



        return fragInstalledNode;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            removeItem(v);
        }

        private void removeItem(View v) {
            int selectedItemPosition = recyclerView.getChildLayoutPosition(v);
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForLayoutPosition(selectedItemPosition);
            TextView textViewName
                    = (TextView) viewHolder.itemView.findViewById(R.id.node_name);
            String selectedName = (String) textViewName.getText();
            int selectedItemId = -1;
            for (int i = 0; i < MyData.nameArray.length; i++) {
                if (selectedName.equals(MyData.nameArray[i])) {
                    selectedItemId = MyData.id_[i];
                }
            }
            removedItems.add(selectedItemId);
            data.remove(selectedItemPosition);
            adapter.notifyItemRemoved(selectedItemPosition);
        }
    }

    private void addRemovedItemToList() {
        int addItemAtListPosition = 3;
        data.add(addItemAtListPosition, new DataModel(
                MyData.nameArray[removedItems.get(0)],
                MyData.versionArray[removedItems.get(0)],
                MyData.id_[removedItems.get(0)],
                MyData.drawableArray[removedItems.get(0)]
        ));
        adapter.notifyItemInserted(addItemAtListPosition);
        removedItems.remove(0);
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
                    version.setText(data.get(position).getVersion());
                    icon_node.setImageResource(data.get(position).getImage());
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
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}