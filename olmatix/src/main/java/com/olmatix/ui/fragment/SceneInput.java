package com.olmatix.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahman on 1/2/2017.
 */

public class SceneInput extends Fragment {
    private static final String[] MSCENE = {"Select Scene", "Scheduled Time", "Home Arrived", "Home Leave", "Base On Sensor", "Nothing"};
    private static ArrayList<SceneModel> data;
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
    private ListView mListView;

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
        mImgAdd = (ImageButton) mView.findViewById(R.id.img_add);
        mSubmitBtn = (Button) mView.findViewById(R.id.btnSubmit);
        mBackBtn = (Button) mView.findViewById(R.id.btnBack);
        mListView= (ListView) mView.findViewById(R.id.listScene);
    }

    private void setupValData() {
        mBundle = getArguments();
        mBundle.getString("sceneName", sceneNamed);
        mTextName.setText(mBundle.getString("sceneName", sceneNamed));
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
                if (validationData() == true) {
                    setSceneData();
                    onBackAction();

                }


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

    private void setupDatabaseRepo() {
        mDbNodeRepo = new dbNodeRepo(getActivity());
        mSceneModel = new SceneModel();
        mDetailNodeModel = new DetailNodeModel();

        mContext = getActivity();
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
            if (mSceneModel.getSceneType() == 0) {
                mSceneModel.setSceneType(mSpinWhat.getSelectedIndex());
            }

        }
        mSceneModel.setSceneName(mBundle.getString("sceneName", sceneNamed));
        mDbNodeRepo.insertDbScene(mSceneModel);
    }








}
