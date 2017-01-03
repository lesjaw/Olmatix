package com.olmatix.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.SceneDetailModel;

import java.util.ArrayList;

/**
 * Created by Rahman on 1/3/2017.
 */

public class SceneSummaryAdapter extends BaseAdapter {
    Activity mContext;
    ArrayList<SceneDetailModel> mSceneDetailData;
    private static LayoutInflater mInflater=null;
    SceneDetailModel mSceneDetail;
    private static dbNodeRepo DbNodeRepo;
    private static ArrayList<DetailNodeModel> data ;
    private TextView sceneId, sceneName, sceneJobs,sceneCmd;
    private ImageButton imgActions;

    public SceneSummaryAdapter(Activity mContext, ArrayList<SceneDetailModel> mSceneDetailData) {
        this.mContext = mContext;
        this.mSceneDetailData = mSceneDetailData;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mSceneDetailData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView = convertView;
        if(convertView == null)
            mView = mInflater.inflate(R.layout.scene_summary_item, null);
        sceneId = (TextView) mView.findViewById(R.id.scene_id);
        sceneName = (TextView) mView.findViewById(R.id.scene_name);
        sceneJobs = (TextView) mView.findViewById(R.id.scene_job);
        sceneCmd = (TextView) mView.findViewById(R.id.scene_command);
        imgActions = (ImageButton) mView.findViewById(R.id.imgActions);

        mSceneDetail= mSceneDetailData.get(position);

        for (int i=0; i > mSceneDetailData.size(); i++){
            sceneId.setText(i);
        }

        DbNodeRepo = new dbNodeRepo(mContext);
        int countDb = DbNodeRepo.getNodeDetailList().size();
        data = new ArrayList<>();
        data.addAll(DbNodeRepo.getNodeDetailList());
       /* for (int k = 0; k < data.size(); k++) {
            z = data.get(k).getNiceName();
            Log.d("DEBUG", "nodeIdData 1: " +  data.get(k).getNiceName());

        }
        if (mSceneDetail.getSceneid() == )
        sceneName.setText(mSceneDetail.getSceneid());
        textView_TanggalLahir.setText(mSceneDetail.getTanggal_lahir());

        if(waris.getJenis_kelamin().equals("1")){
            this.textView_JenisKelamin.setText("Pria");
        } else if(waris.getJenis_kelamin().equals("2")){
            this.textView_JenisKelamin.setText("Wanita");
        }else{
            this.textView_JenisKelamin.setText("");
        }

        this.textView_Hubungan.setText(waris.getHubungan());

*/


        return mView;
    }
}
