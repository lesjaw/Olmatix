package com.olmatix.ui.fragment;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.olmatix.adapter.NodeDetailAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Detail_NodeModel;

import java.util.ArrayList;

/**
 * Created by android on 12/13/2016.
 */

public class Detail_Node extends AppCompatActivity implements OnStartDragListener {

    dbNodeRepo dbNodeRepo;
    String node_id,node_name;
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager layoutManager;
    NodeDetailAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemTouchHelper mItemTouchHelper;
    private Detail_NodeModel detailNodeModel;
    private Paint p = new Paint();
    private TextView label_node;
    private String nicename;
    Detail_Node detail_node;
    private static ArrayList<Detail_NodeModel> data;
    int flagReceiver=0;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_node);

        detail_node =this;
        Intent i = getIntent();
        node_id = i.getStringExtra("node_id");
        node_name = i.getStringExtra("node_name");
        nicename = i.getStringExtra("nice_name");
        data = new ArrayList<>();
        dbNodeRepo =new dbNodeRepo(getApplicationContext());
        detailNodeModel = new Detail_NodeModel();
        setupView();
        setupToolbar();


    }


    private void setupView() {
        label_node = (TextView) findViewById(R.id.label_node);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecycleView    = (RecyclerView) findViewById(R.id.rv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecycleView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailID(node_id));
        adapter = new NodeDetailAdapter(data,node_name, detail_node,this);
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

        if (nicename!=null) {
            label_node.setText(nicename);
        }else{
            label_node.setText(node_name);
        }
    }
    private void setRefresh() {
        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailID(node_id));
        adapter = new NodeDetailAdapter(data,node_name, detail_node,this);
        mRecycleView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setupToolbar(){
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {

        if (flagReceiver==0) {
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
            Log.d("Receiver ", "Detail_Node = Starting..");
            flagReceiver = 1;
        }
        super.onStart();
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
                Log.d("receiver", "NotifyChangeDetail : " + message);
            }
        }
    };



    private void updatelist (){
        adapter.notifyDataSetChanged();
        data.clear();
        data.addAll(dbNodeRepo.getNodeDetailID(node_id));
        if(adapter != null)
        {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());

        }
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

                if (direction == ItemTouchHelper.LEFT)
                {
                    adapter.notifyDataSetChanged();
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecycleView);

                }else{

                    adapter.notifyDataSetChanged();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Detail_Node.this);
                    builder.setTitle("Rename Node detail");

                    final EditText input = new EditText(Detail_Node.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    if (data.get(position).getNice_name_d()!=null) {
                        input.setText(data.get(position).getNice_name_d());
                    }else {
                        input.setText(data.get(position).getName());
                    }
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nice_name = input.getText().toString();
                            detailNodeModel.setNode_id(data.get(position).getNode_id());
                            detailNodeModel.setChannel(data.get(position).getChannel());
                            detailNodeModel.setNice_name_d(nice_name);
                            dbNodeRepo.update_detail_NiceName(detailNodeModel);
                            Toast.makeText(getApplicationContext(),"Successfully Inserted",Toast.LENGTH_LONG).show();
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
                       /* p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);*/
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon action bar is clicked; go to parent activity
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        if (flagReceiver==0) {
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                    mMessageReceiver, new IntentFilter("MQTTStatusDetail"));
            Log.d("Receiver ", "Detail_Node = Starting..");
            flagReceiver = 1;
        }
        super.onPostResume();
    }
}
