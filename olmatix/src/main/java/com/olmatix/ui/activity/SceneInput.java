package com.olmatix.ui.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.olmatix.adapter.SceneDetailAdapter;
import com.olmatix.adapter.SceneSummaryAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;
import com.olmatix.ui.fragment.Scene;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rahman on 1/2/2017.
 */

public class SceneInput extends AppCompatActivity {
    private static final String[] MSCENE = {"Select Scene", "Scheduled Time", "Home Arrived", "Home Leave", "Base On Sensor", "Nothing"};
    private static final String[] LABELDATA= {"Select Schedule", "Date", "Time"};
    static int hour, min;
    ArrayList<SceneModel> dataScene;
    ArrayList<SceneDetailModel> dataDetailScene;
    ArrayList<DetailNodeModel> dataNode;
    SceneDetailAdapter mAdapter;
    private Calendar mCal;
    private int mHour, mMinute, mYear, mMonth, mDay;
    private View mView;
    private Bundle mBundle;
    private TextView mTextName, mSceneTime;
    private MaterialSpinner mSpinWhat, mSpinNode, mSpinnerDialog;
    private SwitchCompat mSwitchNode;
    private ImageButton mImgAdd;
    private String sceneNamed;
    private Button mSubmitBtn, mBackBtn;
    dbNodeRepo mDbNodeRepo;
    SceneModel mSceneModel;
    DetailNodeModel mDetailNodeModel;
    SceneDetailModel mSceneDetailModel;
    private ListView mListView;
    private Boolean isChecked = false;
    private DatePickerDialog mDateDialog;
    private TimePickerDialog mTimeDialog;

    String mDateValue="";
    String mTimeDateValue="";
    String mTimeValue= "";

    Context context;
    Activity mActivity;
    private Toolbar mToolbar;
    private Intent mIntent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_scene_input);
        mActivity = this;
        setupView();
        setupToolbar();
        setupValData();
        setupClickListener();
        setupDatabaseRepo();
        loadSpinnerData();
        loadData();
        setupListViewAdapter();
        
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setupView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextName = (TextView) findViewById(R.id.scene_name);
        mSpinWhat = (MaterialSpinner) findViewById(R.id.spin_what);
        mSpinWhat.setItems(MSCENE);
        mSpinNode = (MaterialSpinner) findViewById(R.id.spin_node);
        mSwitchNode = (SwitchCompat) findViewById(R.id.swich_node);
        mSwitchNode.setChecked(false);
        mImgAdd = (ImageButton) findViewById(R.id.img_add);
        mSubmitBtn = (Button) findViewById(R.id.btnSubmit);
        mBackBtn = (Button) findViewById(R.id.btnBack);
        mListView = (ListView) findViewById(R.id.listScene);
        mSceneTime= (TextView) findViewById(R.id.scene_Time);

    }

    private void setupValData() {
        mIntent = getIntent();
        mIntent.getStringExtra("sceneName");
        mTextName.setText(mIntent.getStringExtra("sceneName"));
        sceneNamed = mIntent.getStringExtra("sceneName");
    }

    private void setupClickListener() {

        mSubmitBtn.setOnClickListener(SubmitBtnClickListener());
        mBackBtn.setOnClickListener(BackBtnClickListener());
        mImgAdd.setOnClickListener(ImgAddClickListener());
        mSpinWhat.setOnItemSelectedListener(SpinWhatItemClickListener());
    }

    private MaterialSpinner.OnItemSelectedListener SpinWhatItemClickListener() {
        return new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                for (int i = 0; i < position; i++) {
                    int dataSpinner = i;
                    if(item.equals("Scheduled Time")){
                        showDateTimeDialog();
                        mSceneModel.setSceneType(i);
                    } else {
                        mSceneModel.setSceneType(i);
                    }
                }
            }
        };
    }

    private View.OnClickListener ImgAddClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillDataScene();

                clearSceneInputForm();

            }
        };
    }

    private void setupListViewAdapter() {
        dataDetailScene = mDbNodeRepo.getSceneDetailList();
        dataDetailScene = new ArrayList<SceneDetailModel>();
        Log.d("DEBUG", "setupListViewAdapter: " +dataDetailScene.size());
        mAdapter = new SceneDetailAdapter(mActivity, dataDetailScene);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mListView.requestFocus();
    }

    private void clearSceneInputForm(){
        mSpinWhat.setSelectedIndex(0);
        mSpinNode.setSelectedIndex(0);
        mSwitchNode.setChecked(false);


    }

    private View.OnClickListener SubmitBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //reloadScene();
                setSceneData();
                //onBackAction();

               /* if (validationData() == true) {


                }*/


            }
        };
    }

    private View.OnClickListener BackBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackAction();

            }
        };
    }


    private void fillDataScene() {
        mSceneDetailModel = new SceneDetailModel();
        int sceneDbCount = mDbNodeRepo.getSceneList(sceneNamed).size();
        Log.d("DEBUG", "INSERT SCENE " + sceneNamed);
        dataScene = new ArrayList<>();
        mSceneModel.setSceneName(sceneNamed);
        dataScene.addAll(mDbNodeRepo.getSceneList(sceneNamed));
        if (sceneDbCount != 0) {
            Log.d("DEBUG", "INSERT SCENE " + sceneDbCount);

            for (int w = 0; w < dataScene.size(); w++) {
                int mSceneId = dataScene.get(w).getId();
                Log.d("DEBUG", "VAL SCENE: " + mSceneId);
                mSceneDetailModel.setSceneid(mSceneId);
            }
        }
        for (int i = 0; i < mSpinNode.length(); i++) {
            String mPath = String.valueOf(mSpinNode.getSelectedIndex());
            mSceneDetailModel.setPath(mPath);
        }

        if (isChecked == true) {
            mSceneDetailModel.setCommand("ON");
        } else {
            mSceneDetailModel.setCommand("OFF");
        }
    }

    private void setupDatabaseRepo() {
        mDbNodeRepo = new dbNodeRepo(mActivity);
        mSceneModel = new SceneModel();
        mDetailNodeModel = new DetailNodeModel();
        mSceneDetailModel = new SceneDetailModel();
    }

    private void loadSpinnerData() {
        List<SpinnerObject> nodeLabel = mDbNodeRepo.getAllLabels();
        if (mSpinNode != null) {
            mSpinNode.setItems(nodeLabel);
            Log.d("DEBUG", "loadSpinnerData: "+ nodeLabel.toArray());
        }

    }

    private boolean validationData() {

        if (mSpinWhat.getSelectedIndex() == 0) {
            Toast.makeText(mActivity, "Please select your base on what options!..", Toast.LENGTH_SHORT).show();
            mSpinWhat.setFocusableInTouchMode(true);
            mSpinWhat.requestFocus();
            mSpinWhat.setFocusableInTouchMode(false);
            return false;
        }

        if (mSpinNode.getSelectedIndex() == 0) {
            Toast.makeText(mActivity, "Please select your node options!..", Toast.LENGTH_SHORT).show();
            mSpinWhat.setFocusableInTouchMode(true);
            mSpinWhat.requestFocus();
            mSpinWhat.setFocusableInTouchMode(false);
            return false;
        }

        dataDetailScene = mDbNodeRepo.getSceneDetailList();
        if (dataDetailScene != null) {
            if (dataDetailScene.size() == 0) {
                Toast.makeText(mActivity, "Ups, Sorry your scene list is empty.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }


        return true;
    }



    private void setSceneData() {
        setupValData();
        for (int i = 0; i < mSpinWhat.length(); i++) {
            int sceneType = mSpinWhat.getSelectedIndex();
            mSceneModel.setSceneType(sceneType);
            if (sceneType == 1){
                //showDateTimeDialog();
                for (int x = 0; x < LABELDATA.length; x++ ){
                    int mTimeData = x;
                    if (mTimeData == 1){
                        System.out.println("Time and Date Value == "+mTimeDateValue);
                        mSceneModel.setSchedule(mTimeDateValue);
                    } else if (mTimeData == 2){
                        System.out.println("Time Value == "+mTimeValue);
                        mSceneModel.setSchedule(mTimeDateValue);
                    }
                }
            }
        }

        mSceneModel.setSceneName(mBundle.getString("sceneName", sceneNamed));
        mDbNodeRepo.insertDbScene(mSceneModel);
    }



    public void reloadSceneDetail() {
        Thread thread = new Thread(null, loadSceneDetail);
        thread.start();
        setupClickListener();

    }

    private Runnable loadSceneDetail = new Runnable() {

        @Override
        public void run() {
            try {
                dataDetailScene = mDbNodeRepo.getSceneDetailList();
                Log.d("DEBUG", "DATA SCENE COUNT=" + String.valueOf(dataDetailScene.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(updateListView);
        }

    };


    private void loadData(){
        dataScene = mDbNodeRepo.getAllSceneList();
        fillDataScene();
    }
    private Runnable updateListView = new Runnable() {

        @Override
        public void run() {
            Log.d("DEBUG", "run: "+ mAdapter);
            dataDetailScene = mDbNodeRepo.getSceneDetailList();
            mAdapter = new SceneDetailAdapter(mActivity, dataDetailScene);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mListView.requestFocus();
        }

    };


    private void showDateTimeDialog(){

        mSpinnerDialog = new MaterialSpinner(mActivity);

        mSpinnerDialog.setItems(LABELDATA);
        mSpinnerDialog.setDropdownHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mSpinnerDialog.setDropdownMaxHeight(350);

        new AlertDialog.Builder(mActivity)
                .setTitle("Time Schedule on what")
                .setMessage("Please select out base time schedule..")
                .setView(mSpinnerDialog)
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("DEBUG", "onClick: " + mSpinnerDialog.getSelectedIndex());
                        if (mSpinnerDialog.getSelectedIndex() == 1){
                            showDatePicker();

                        } else if(mSpinnerDialog.getSelectedIndex() == 2){
                            showTimeDialog();
                            Log.d("DEBUG", "onClick: " + mTimeValue);
                        }else{
                            dialog.dismiss();
                        }

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();


    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        mDateDialog = new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                mDateValue = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                timePicker();
            }

        }, mYear, mMonth, mDay);
        mDateDialog.setTitle("Select Date");
        mDateDialog.show();
    }

    private void showTimeDialog() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mTimeDialog =  new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                mTimeValue = hourOfDay + ":" + minute;
                mSceneTime.setText(mTimeValue);


            }
        }, mHour, mMinute, true);
        mTimeDialog.setTitle("Select Time");
        mTimeDialog.show();
    }

    private void timePicker() {

        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mTimeDialog =  new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mTimeDateValue = mDateValue+" "+hourOfDay + ":" + minute;
                mSceneTime.setText(mTimeDateValue);

            }
        }, mHour, mMinute, true);
        mTimeDialog.setTitle("Select Time");
        mTimeDialog.show();
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
