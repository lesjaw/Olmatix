package com.olmatix.lesjaw.olmatix;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;

public class Installed_Node extends Fragment {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;
    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;
    private int _Node_Id=0;



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
                                //Toast.makeText(getContext(), "Your Product ID -> "+ String.valueOf(inputResult), Toast.LENGTH_SHORT).show();

                                dbNodeRepo repo = new dbNodeRepo(getContext());

                                ArrayList<HashMap<String, String>> nodeList =  repo.getNodeList();
                                if(nodeList.size()!=0) {

                                    _Node_Id = 0;
                                    Intent intent = getActivity().getIntent();
                                    _Node_Id = intent.getIntExtra("node_Id", 0);
                                    repo.getNodeById(_Node_Id);

                                    Toast.makeText(getContext(), "Node DB ID -> "+ String.valueOf(_Node_Id), Toast.LENGTH_SHORT).show();



                                }else{
                                    Toast.makeText(getContext(),"No Node!",Toast.LENGTH_SHORT).show();
                                    if (_Node_Id==0) {
                                       // _Node_Id = repo.insert(inputResult);
                                    }
                                }

                                String topic = "devices/"+inputResult+"/$fwname";
                                int qos = 1;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                                    subToken.setActionCallback(new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                            Toast.makeText(getContext(), "Sub Success", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(IMqttToken asyncActionToken,
                                                              Throwable exception) {
                                            // The subscription could not be performed, maybe the user was not
                                            // authorized to subscribe on the specified topic e.g. using wildcards
                                            Toast.makeText(getContext(), "Sub fail", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }


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