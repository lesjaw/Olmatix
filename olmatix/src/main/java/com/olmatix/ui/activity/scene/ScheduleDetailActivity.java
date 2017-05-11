package com.olmatix.ui.activity.scene;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.olmatix.adapter.DayAdapter;
import com.olmatix.adapter.SceneDetailAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.HorizontalListView;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;

import java.util.ArrayList;

/**
 * Created by android on 4/15/2017.
 */

public class ScheduleDetailActivity extends AppCompatActivity {

    private boolean[] weekDays;
    private TimePickerDialog mTimePicker;
    private Pair<Integer, Integer> time;
    private Activity mActivity;
    private Context mContext;
    private View mInflater;
    private TextView mTimeTxt, mSceneMode;
    private Toolbar mToolbar;
    private String mSceneData;
    private int mSceneIdData = 0;
    private Intent mIntent;
    private dbNodeRepo mDbNodeRepo;
    private SceneDetailModel mSceneModel;
    private DetailNodeModel mDetailNodeModel;
    private SceneDetailModel mSceneDetailModel;
    private LinearLayout mDayLayout, mTimeDayLayout;
    private View mViewDash;
    private MaterialSpinner mSpinNode;
    ArrayList<SceneDetailModel> sceneDetailList;
    private ImageButton btnAdd;
    private SceneDetailAdapter mSceneDetailAdapter;
    private ListView listSceneDetailData;
    private HorizontalListView mDayRv;
    private DayAdapter mDayAdapter;
    private LinearLayoutManager dayLinearManager;
    private ArrayList<String> mDayList;
    String mDayValue = null;
    public boolean stateChanged = false;
    private int mSelectedPosition= -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);
        mActivity = this;
        runThread();

        initView();
        setupToolbar();

        setClickListeners();
        setupDatabases();
        mLoadSpinnerData();



    }



    private void initView() {
        time = new Pair<>(8, 30);
        setTimePicker(8, 30);

        mTimeTxt = (TextView) findViewById(R.id.time);
        mViewDash = (View) findViewById(R.id.view_dash1);
        mTimeDayLayout = (LinearLayout) findViewById(R.id.label_layout);
        mDayLayout = (LinearLayout) findViewById(R.id.dayLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSpinNode = (MaterialSpinner) findViewById(R.id.spin_node);
        btnAdd = (ImageButton) findViewById(R.id.img_add);
        mSceneMode = (TextView) findViewById(R.id.txt_sceneid);
        listSceneDetailData = (ListView) findViewById(R.id.listData);
        mDayRv= (HorizontalListView) findViewById(R.id.listview);
       /* mDayAdapter = new DayAdapter(this);
        mDayRv.setAdapter(mDayAdapter);
        mDayAdapter.notifyDataSetChanged();*/

    }





    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDatabases() {
        mDbNodeRepo = new dbNodeRepo(mActivity);
        mSceneModel = new SceneDetailModel();
        mDetailNodeModel = new DetailNodeModel();
        mSceneDetailModel = new SceneDetailModel();
        reloadSceneData();
    }

    private void mLoadSpinnerData() {
        ArrayList<SpinnerObject> nodeLabel = mDbNodeRepo.getAllLabels();
        ArrayList<String> arrayList= new ArrayList<>();;
        for(int i=0; i<nodeLabel.size(); i++)
        {
            arrayList.add(nodeLabel.get(i).getDatabaseValue());
        }
        Log.e("array List",arrayList+"");

        if (arrayList.isEmpty()) {
            MaterialDialog.Builder mBuilderSpin = new MaterialDialog.Builder(mActivity);
            mBuilderSpin.title("Warning");
            mBuilderSpin.iconRes(R.drawable.ic_warning);
            mBuilderSpin.limitIconToDefaultSize();
            mBuilderSpin.content("We dont found any node in here. Please add some node!!");
            mBuilderSpin.positiveText("OK");
            mBuilderSpin.show();
        } else {
            mSpinNode.setItems(arrayList);

            Log.d("DEBUG", "loadSpinnerData: " + nodeLabel.toArray());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void runThread() {

        new Thread() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            initLoadDb();
                            initIntent();
                        }
                    });
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initIntent() {
        mSceneData = getIntent().getStringExtra("SCENETYPE");
        mSceneIdData = getIntent().getIntExtra("SCENEID", 0);
        mSceneMode.setText("Scene Name :" + mSceneData + " - " + mSceneIdData);
        if (mSceneIdData != 0) {
            mDayLayout.setVisibility(View.GONE);
            mTimeDayLayout.setVisibility(View.GONE);
            mViewDash.setVisibility(View.GONE);

        }
    }

    private void initLoadDb() {
        mDbNodeRepo = new dbNodeRepo(mActivity);
        mSceneModel = new SceneDetailModel();
        mDetailNodeModel = new DetailNodeModel();
        mSceneDetailModel = new SceneDetailModel();
    }

    private void setTimePicker(int hour, int minutes) {
        mTimePicker = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setTime(hourOfDay, minute);
                    }
                }, hour, minutes, true);
    }

    private void setTime(int hour, int minutes) {
        time = new Pair<>(hour, minutes);
        String hourString = ((time.first > 9) ?
                String.valueOf(time.first) : ("0" + time.first));
        String minutesString = ((time.second > 9) ?
                String.valueOf(time.second) : ("0" + time.second));
        String time = hourString + ":" + minutesString;
        mTimeTxt.setText(time);
    }


    public void reloadSceneData() {
        Thread thread = new Thread(null, loadSceneData);
        thread.start();
        this.setClickListeners();

    }



    private void setClickListeners() {
        btnAdd.setOnClickListener(addBtnClickListener());
        mTimeTxt.setOnClickListener(mTimeClickListener());
        mDayRv.registerListItemClickListener(mItemClickListener());

    }

    private HorizontalListView.OnListItemClickListener mItemClickListener() {
        return (v, position) -> {
            Log.d("TAG", "onClick: " + v +"\n\n"+ position);
            v.setSelected(true);
            if (position == 0){
                mSceneModel.setSunday("1");
            } else if (position == 1){
                mSceneModel.setMonday("1");
            }else if (position == 2){
                mSceneModel.setTuesday("1");
            }else if (position == 3){
                mSceneModel.setWednesday("1");
            }else if (position == 4){
                mSceneModel.setThursday("1");
            }else if (position == 5){
                mSceneModel.setFriday("1");
            }else if (position == 6){
                mSceneModel.setSaturday("1");
            }else{
                v.setSelected(false);
                mSceneModel.setSunday("");
                mSceneModel.setMonday("");
                mSceneModel.setTuesday("");
                mSceneModel.setWednesday("");
                mSceneModel.setThursday("");
                mSceneModel.setFriday("");
                mSceneModel.setSaturday("");
            }
        };
    }


    private View.OnClickListener mTimeClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimePicker.show();
            }
        };
    }


    private View.OnClickListener addBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataScene();
                //mDbNodeRepo.addWaris(waris);
                //Log.d("DEBUG", "Waris ADDED");
                //reloadWaris();
                //clearWarisInputForm();
            }
        };
    }

    private void setDataScene() {
        //mSceneModel = new SceneModel();
        mSceneModel.setSceneName(mSceneData);
        mSceneModel.setSceneType(mSceneIdData);
        //

        if (mSceneIdData == 0) {
            String timeData = mTimeTxt.getText().toString();
            String[] outputTime = timeData.split(":");
            mSceneModel.setHour(Integer.parseInt(outputTime[0]));
            mSceneModel.setMin(Integer.parseInt(outputTime[1]));
            mSceneModel.setLocation("");
            mSceneModel.setSensor("");
            mDbNodeRepo.insertDbScene(mSceneModel);

        } else {
            mSceneModel.setHour(00);
            mSceneModel.setMin(00);

            mSceneModel.setLocation("");
            mSceneModel.setSensor("");
            mSceneModel.setLocation("Null");
            mSceneModel.setSensor("Null");
            mDbNodeRepo.insertDbScene(mSceneModel);
        }

    }


    private Runnable loadSceneData = new Runnable() {

        @Override
        public void run() {
            try {
                sceneDetailList = mDbNodeRepo.getSceneDetailList();
                Log.d("DEBUG", "DATA WARIS COUNT=" + String.valueOf(sceneDetailList.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(updateListView);
        }
    };


    private Runnable updateListView = new Runnable() {

        @Override
        public void run() {
            mSceneDetailAdapter = new SceneDetailAdapter(mActivity, sceneDetailList);
            listSceneDetailData.setAdapter(mSceneDetailAdapter);
            mSceneDetailAdapter.notifyDataSetChanged();
            listSceneDetailData.requestFocus();
        }

    };

}
