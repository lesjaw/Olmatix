package com.olmatix.ui.activity.scene;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.olmatix.service.AlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.fragments.BackConfirmationFragment;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

import static java.security.AccessController.getContext;

/**
 * Created by Rahman on 1/13/2017.
 */

public class ScheduleActivity extends AppCompatActivity {

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
    private SceneModel mSceneModel;
    private DetailNodeModel mDetailNodeModel;
    private SceneDetailModel mSceneDetailModel;
    private LinearLayout mDayLayout, mTimeDayLayout;
    private View mViewDash;
    private Spinner mSpinNode;
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
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);
        mActivity = this;
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        runThread();
        setupDatabases();

        initView();
        setupToolbar();
        setClickListeners();
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
        mSpinNode = (Spinner) findViewById(R.id.spin_node);
        btnAdd = (ImageButton) findViewById(R.id.img_add);
        mSceneMode = (TextView) findViewById(R.id.txt_sceneid);
        listSceneDetailData = (ListView) findViewById(R.id.listData);
        mDayRv= (HorizontalListView) findViewById(R.id.listview);
        mDayAdapter = new DayAdapter(this);
        mDayRv.setAdapter(mDayAdapter);
        mDayAdapter.notifyDataSetChanged();

    }


    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDatabases() {
        mDbNodeRepo = new dbNodeRepo(mActivity);
        mSceneModel = new SceneModel();
        mDetailNodeModel = new DetailNodeModel();
        mSceneDetailModel = new SceneDetailModel();
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
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ScheduleActivity.this, android.R.layout.simple_list_item_1, arrayList); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpinNode.setAdapter(spinnerArrayAdapter);
            mSpinNode.setSelection(0,true);
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
        mSceneModel = new SceneModel();
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


                for(int i=0; i<mDbNodeRepo.getAllSceneList().size(); i++) {
                    if (mSceneModel.getMonday() != null) {
                        if (mSceneModel.getMonday().equals("1")) {
                            forday(Calendar.MONDAY, mSceneModel.getHour(), mSceneModel.getMin());
                        }
                    }
                    if (mSceneModel.getThursday() != null) {
                        if (mSceneModel.getThursday().equals("1")) {
                            forday(Calendar.THURSDAY, mSceneModel.getHour(), mSceneModel.getMin());
                        }
                    }
                    if (mSceneModel.getWednesday() != null) {
                        if (mSceneModel.getWednesday().equals("1")) {
                            forday(Calendar.WEDNESDAY, mSceneModel.getHour(), mSceneModel.getMin());
                        }
                    }
                    if (mSceneModel.getTuesday() != null)
                    {
                    if (mSceneModel.getTuesday().equals("1")) {
                        forday(Calendar.TUESDAY, mSceneModel.getHour(), mSceneModel.getMin());
                    }
                    }
                    if(mSceneModel.getFriday() != null)
                    {
                    if (mSceneModel.getFriday().equals("1")) {
                        forday(Calendar.FRIDAY, mSceneModel.getHour(), mSceneModel.getMin());
                    }
                    }
                    if(mSceneModel.getSaturday() != null) {
                        if (mSceneModel.getSaturday().equals("1")) {
                            forday(Calendar.SATURDAY, mSceneModel.getHour(), mSceneModel.getMin());
                        }
                    }
                    if(mSceneModel.getSunday() != null) {
                        if (mSceneModel.getSunday().equals("1")) {
                            forday(Calendar.SUNDAY, mSceneModel.getHour(), mSceneModel.getMin());
                        }
                    }
                }
                Toast.makeText(getApplicationContext(),"Alram set successfull !",Toast.LENGTH_LONG).show();

                   // alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

            }
        };
    }

    public void forday(int week,int hour,int minuts) {
        Intent myIntent = new Intent(ScheduleActivity.this, AlarmReceiver.class);

        // A PendingIntent specifies an action to take in the
        // future
        Calendar calSet = Calendar.getInstance();
        calSet.set(Calendar.DAY_OF_WEEK, week);
        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, minuts);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);
        pendingIntent = PendingIntent.getBroadcast(ScheduleActivity.this, week, myIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(),AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private void setDataScene() {
        //mSceneModel = new SceneModel();
        mSceneModel.setSceneName(mSceneData);
        mSceneModel.setSceneType(mSceneIdData);
        mSceneModel.setNode_id(mSpinNode.getSelectedItem().toString());

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

    /*public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayHolder> {

        private List<String> dayList;

        public class DayHolder extends RecyclerView.ViewHolder {
            public TextView mDayText;

            public DayHolder(View view) {
                super(view);
                mDayText = (TextView) view.findViewById(R.id.day);

            }
        }


        public DayAdapter(List<String> dayList) {
            this.dayList = dayList;
        }

        @Override
        public DayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.days, parent, false);

            return new DayHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DayHolder holder, int position) {

            holder.mDayText.setText(mDayList.get(position));




            holder.mDayText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                   // Toast.makeText(MainActivity.this,holder.txtView.getText().toString(),Toast.LENGTH_SHORT).show();
                    if(stateChanged){
                        holder.mDayText.setBackgroundResource(0);
                        //mDayValue = "1";
                    } else {

                        holder.mDayText.setBackgroundResource(R.drawable.ic_check);
                        //mDayValue = null;
                    }
                    stateChanged = !stateChanged;
                    Log.d("TAG", "onClick: " + stateChanged);
                    mClickEvent(stateChanged);

                }
            });
        }

        @Override
        public int getItemCount() {
            return dayList.size();
        }
    }*/
}
