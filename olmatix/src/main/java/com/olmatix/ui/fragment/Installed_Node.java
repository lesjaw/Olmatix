package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.olmatix.adapter.CustomAdapter;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.database.dbNode;
import com.olmatix.utils.Connection;
import com.olmatix.model.MyData;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DataModel;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.Attributes;

public class Installed_Node extends Fragment {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;
    public static View.OnClickListener myOnClickListener;
    public static View.OnClickListener MyOnClickButListener;
    private static ArrayList<Integer> removedItems;
    //private int _Node_Id=0;
    private String _Node_Name;



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

                                String topic = "devices/"+inputResult+"/$online";
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

        myOnClickListener = new MyOnClickListener(getContext());

        return fragInstalledNode;
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
}