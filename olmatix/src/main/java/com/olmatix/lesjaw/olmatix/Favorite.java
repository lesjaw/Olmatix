package com.olmatix.lesjaw.olmatix;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class Favorite extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mFavourit = inflater.inflate(R.layout.frag_favorite, container, false);
        Button nxtact1 = (Button) mFavourit.findViewById(R.id.testBut1);

        FloatingActionButton fab = (FloatingActionButton) mFavourit.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Toast.makeText(getContext(), "Nothing to add", Toast.LENGTH_SHORT).show();
            }
        });

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