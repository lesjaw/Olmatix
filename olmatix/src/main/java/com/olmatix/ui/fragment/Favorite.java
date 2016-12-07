package com.olmatix.ui.fragment;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class Favorite extends Fragment {
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

                                String topic = "devices/"+inputResult+"/#";
                                try {
                                    Connection.getClient().unsubscribe(topic);
                                    /*IMqttToken subToken = Connection.getClient().unsubscribe(topic);
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
                                });*/
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
        Button nxtact1 = (Button) mFavourit.findViewById(R.id.testBut1);
        nxtact1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.getClient().isConnected()) {
                    String topic = "devices/809ed5e0/light/0/set";
                    String payload = "ON";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setQos(1);
                        message.setRetained(true);
                        Connection.getClient().publish(topic, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getContext(), "Not connected to server", Toast.LENGTH_LONG).show();

            }
        });

        Button  nxtact2 = (Button) mFavourit.findViewById(R.id.testBut2);
        nxtact2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Connection.getClient().isConnected()) {
                    String topic = "devices/809ed5e0/light/0/set";
                    String payload = "OFF";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setQos(1);
                        message.setRetained(true);
                        Connection.getClient().publish(topic, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getContext(), "Not connected to server", Toast.LENGTH_LONG).show();
            }

        });


        ((TextView)mFavourit.findViewById(R.id.text)).setText("Lampu Kamar");
        return mFavourit;
    }}