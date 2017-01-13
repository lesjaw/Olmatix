package com.olmatix.ui.fragment;

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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.olmatix.adapter.SceneAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.OnStartDragListener;
import com.olmatix.helper.SimpleItemTouchHelperCallback;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.SceneModel;
import com.olmatix.ui.activity.SceneActivity;
import com.olmatix.ui.activity.scene.ScheduleActivity;

import java.util.ArrayList;

import static com.olmatix.lesjaw.olmatix.R.id.fab;

/**
 * Created by Rahman on 12/18/2016.
 */
public class Scene extends Fragment implements OnStartDragListener {

    private static String TAG = Scene.class.getSimpleName();
    public static dbNodeRepo dbNodeRepo;
    private static ArrayList<SceneModel> data;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private View mView;
    private FloatingActionButton mFab;
    private RecyclerView mRecycleView;
    private SceneAdapter adapter;
    private Paint p = new Paint();
    Context scene_node;
    public static final int NEW_ALARM = 1;
    private ListView mListScenetype;
    private Intent mIntent;
    private String sceneType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.frag_scene, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbNodeRepo = new dbNodeRepo(getActivity());
        data = new ArrayList<>();

        scene_node=getContext();
        setupView();
        onClickListener();

        mRecycleView.addOnItemTouchListener(new Scene.RecyclerTouchListener(getActivity(),
                mRecycleView, new Scene.ClickListener() {


                    @Override
                    public void onClick(View view, int position) {

                    }

                    @Override
                    public void onLongClick(View view, int position) {

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
                mListScenetype = new ListView(getActivity());
                String[] sceneListItem = {"Scheduled Time", "Home Location", "Base On Sensor", "Manual Triger"};
                ArrayAdapter<String> listTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.scene_type_item, sceneListItem);
                mListScenetype.setAdapter(listTypeAdapter);
                mListScenetype.setPadding(4,0,4,0);

                int totalHeight = 0;
                for (int i = 0; i < listTypeAdapter.getCount(); i++) {
                    View listItem = listTypeAdapter.getView(i, null, mListScenetype);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.height = totalHeight + (mListScenetype.getDividerHeight() * (listTypeAdapter.getCount() - 1));
                Log.d("DEBUG", "createConnectTitleStep: " + params);

                mListScenetype.setLayoutParams(params);
                mListScenetype.requestLayout();

                mListScenetype.setAdapter(listTypeAdapter);
                // Set grid view to alertDialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(mListScenetype);
                builder.setTitle("Pick Scene Type");
                final AlertDialog ad = builder.show();

                mListScenetype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        view.setSelected(true);
                        if(position == 0){
                            sceneType = mListScenetype.getItemAtPosition(position).toString();
                            Log.d(TAG, "onItemClick: " + sceneType);

                            mIntent = new Intent(getContext(), ScheduleActivity.class);
                            mIntent.putExtra("scene", sceneType);
                            startActivity(mIntent);
                        } else {

                        }
                    }
                });
                /*
                Intent intent = new Intent(getContext(), SceneActivity.class);
                startActivityForResult(intent, NEW_ALARM);*/
            }

        };
    }

    private void setupView() {
        mRecycleView = (RecyclerView) mView.findViewById(R.id.rv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipeRefreshLayout);

        mFab = (FloatingActionButton) mView.findViewById(fab);

        mRecycleView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        data.clear();
        data.addAll(dbNodeRepo.getScene());
        adapter = new SceneAdapter(data,scene_node,this);
        mRecycleView.setAdapter(adapter);

        initSwipe();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecycleView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                setRefresh();

            }
        });

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && mFab.isShown()) {
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
    private void setAdapter(){
        data.clear();
        data.addAll(dbNodeRepo.getScene());
        adapter = new SceneAdapter(data,scene_node,this);
        mRecycleView.setAdapter(adapter);

    }
    private void setRefresh() {

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete this Node?");
                    builder.setMessage("del?");


                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

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

                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

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
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
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

    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private Scene.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final Scene.ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
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
