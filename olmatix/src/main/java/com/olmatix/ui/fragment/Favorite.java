package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Favorite extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View mFavourit = inflater.inflate(R.layout.frag_favorite, container, false);

        FloatingActionButton fab = (FloatingActionButton) mFavourit.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final EditText m_Text = new EditText(getContext());

                new AlertDialog.Builder(getContext())
                        .setTitle("Add Node")
                        .setMessage("Please type Olmatix product ID!")
                        .setView(m_Text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputResult = m_Text.getText().toString();

                                String topic = "devices/"+inputResult+"/$online";
                                try {
                                    Connection.getClient().unsubscribe(topic);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                                String topic1 = "devices/"+inputResult+"/#";
                                try {
                                    Connection.getClient().unsubscribe(topic1);
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


        return mFavourit;
    }

}