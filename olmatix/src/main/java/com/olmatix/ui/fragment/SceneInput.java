package com.olmatix.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.olmatix.adapter.SceneSummaryAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Rahman on 1/2/2017.
 */

public class SceneInput extends Fragment {
    private static final String[] MSCENE = {"Select Scene", "Scheduled Time", "Home Arrived", "Home Leave", "Base On Sensor", "Nothing"};
    private static ArrayList<SceneModel> dataScene;
    private static ArrayList<SceneDetailModel> dataDetailScene;
    private static ArrayList<DetailNodeModel> dataNode;
    private View mView;
    private Bundle mBundle;
    private TextView mTextName;
    private MaterialSpinner mSpinWhat, mSpinNode;
    private SwitchCompat mSwitchNode;
    private ImageButton mImgAdd;
    private String sceneNamed;
    private Button mSubmitBtn, mBackBtn;
    private dbNodeRepo mDbNodeRepo;
    private SceneModel mSceneModel;
    private Context mContext;
    private DetailNodeModel mDetailNodeModel;
    private SceneDetailModel mSceneDetailModel;
    private ListView mListView;
    private Boolean isChecked = false;

    static int hour, min;

    java.sql.Time timeValue;
    SimpleDateFormat mFormat;
    Calendar mCal;
    int year, month, day;
    SimpleDateFormat mFormatter;

    public SceneInput() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_scene_input, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView();
        setupValData();
        setupClickListener();
        setupDatabaseRepo();
        loadSpinnerData();

    }

    private void setupView() {
        mTextName = (TextView) mView.findViewById(R.id.scene_name);
        mSpinWhat = (MaterialSpinner) mView.findViewById(R.id.spin_what);
        mSpinWhat.setItems(MSCENE);
        mSpinNode = (MaterialSpinner) mView.findViewById(R.id.spin_node);
        mSwitchNode = (SwitchCompat) mView.findViewById(R.id.swich_node);
        mSwitchNode.setChecked(false);
        mImgAdd = (ImageButton) mView.findViewById(R.id.img_add);
        mSubmitBtn = (Button) mView.findViewById(R.id.btnSubmit);
        mBackBtn = (Button) mView.findViewById(R.id.btnBack);
        mListView= (ListView) mView.findViewById(R.id.listScene);
    }

    private void setupValData() {
        mBundle = getArguments();
        mBundle.getString("sceneName", sceneNamed);
        mTextName.setText(mBundle.getString("sceneName", sceneNamed));
        sceneNamed = mBundle.getString("sceneName", sceneNamed);
        Log.d("DEBUG", "setupValData: "+ sceneNamed);
    }

    private void setupClickListener() {

        mSubmitBtn.setOnClickListener(SubmitBtnClickListener());
        mBackBtn.setOnClickListener(BackBtnClickListener());
        mImgAdd.setOnClickListener(ImgAddClickListener());
    }



    private View.OnClickListener ImgAddClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    private View.OnClickListener SubmitBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //setSceneData();
                //fillDataScene();
                reloadScene();
                onBackAction();
                /*if (validationData() == true) {


                }*/


            }
        };
    }

    private View.OnClickListener BackBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Scene mScene = new Scene();
                ft.replace(R.id.frame_container, mScene);
                ft.addToBackStack(null);
                ft.commit();

            }
        };
    }

    private void fillDataScene(){
        //sceneNamed =
        int sceneDbCount = mDbNodeRepo.getSceneList(sceneNamed).size();
        Log.d("DEBUG", "INSERT SCENE " + sceneNamed);
        dataScene = new ArrayList<>();
        mSceneModel.setSceneName(sceneNamed);
        dataScene.addAll(mDbNodeRepo.getSceneList(sceneNamed));
        if (sceneDbCount !=0){
            Log.d("DEBUG", "INSERT SCENE " + sceneDbCount);

            for (int w = 0; w < dataScene.size(); w++){
                int mSceneId = dataScene.get(w).getId();
                Log.d("DEBUG", "VAL SCENE: " + mSceneId);
                mSceneDetailModel.setSceneid(mSceneId);
            }
        }
        for (int i = 0; i < mSpinNode.length(); i++) {
            String mPath = String.valueOf(mSpinNode.getSelectedIndex());
            mSceneDetailModel.setPath(mPath);
        }
        if (isChecked == true){
            mSceneDetailModel.setCommand("ON");
        } else {
            mSceneDetailModel.setCommand("OFF");
        }

        mDbNodeRepo.insertSceneDetail(mSceneDetailModel);

    }


    private void setupDatabaseRepo() {
        mDbNodeRepo = new dbNodeRepo(getActivity());
        mSceneModel = new SceneModel();
        mDetailNodeModel = new DetailNodeModel();
        mSceneDetailModel =  new SceneDetailModel();
    }




    private void loadSpinnerData() {
        List<SpinnerObject> nodeLabel = mDbNodeRepo.getAllLabels();
        if (mSpinNode != null) {
            mSpinNode.setItems(nodeLabel);
        }

    }

    private boolean validationData() {

        if (mSpinWhat.getSelectedIndex() == 0) {
            Toast.makeText(getActivity(), "Please select your base on what options!..", Toast.LENGTH_SHORT).show();
            mSpinWhat.setFocusableInTouchMode(true);
            mSpinWhat.requestFocus();
            mSpinWhat.setFocusableInTouchMode(false);
            return false;
        }

        if (mSpinNode.getSelectedIndex() == 0) {
            Toast.makeText(getActivity(), "Please select your node options!..", Toast.LENGTH_SHORT).show();
            mSpinWhat.setFocusableInTouchMode(true);
            mSpinWhat.requestFocus();
            mSpinWhat.setFocusableInTouchMode(false);
            return false;
        }

        dataDetailScene = mDbNodeRepo.getAllScene();
        if(dataDetailScene != null){
            if(dataDetailScene.size() == 0){
                Toast.makeText(getActivity(), "Ups, Sorry your scene list is empty.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }




        return true;
    }

    private void onBackAction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Scene mScene = new Scene();
        ft.replace(R.id.frame_container, mScene);
        ft.addToBackStack(null);
        ft.commit();
    }


    private void setSceneData() {
        mBundle = getArguments();
        for (int i = 0; i < mSpinWhat.length(); i++) {
            if(mSpinWhat.length() == 1 ){
                //loadDateTimePicker();
            }
            
            if (mSceneModel.getSceneType() == 0) {
                mSceneModel.setSceneType(mSpinWhat.getSelectedIndex());
                
            } 

        }

        mSceneModel.setSceneName(mBundle.getString("sceneName", sceneNamed));
        mDbNodeRepo.insertDbScene(mSceneModel);
    }

    public void reloadScene(){
        Thread thread = new Thread(null, loadScene);
        thread.start();
        //setupClickListener();

    }

    private Runnable loadScene = new Runnable() {

        @Override
        public void run() {
            try {
                setSceneData();
                dataScene = mDbNodeRepo.getSceneList(sceneNamed);
                Log.d("DEBUG", "DATA SCENE COUNT=" + String.valueOf(dataScene.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(insertSceneDetail);
        }

    };


    private Runnable insertSceneDetail = new Runnable() {

        @Override
        public void run() {
            fillDataScene();
           /* warisAdapter = new WarisAdapter(activity, warisList);
            listView_DataWaris.setAdapter(warisAdapter);
            warisAdapter.notifyDataSetChanged();
            listView_DataWaris.requestFocus();*/
        }

    };

}
