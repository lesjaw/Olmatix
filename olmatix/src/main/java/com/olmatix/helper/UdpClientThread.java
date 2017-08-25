package com.olmatix.helper;

/**
 * Created by USER on 12/08/2017.
 */

import android.os.Message;
import android.util.Log;

import com.olmatix.adapter.NodeDashboardAdapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClientThread extends Thread{

    String dstAddress;
    int dstPort;
    String command;
    private boolean running;
    NodeDashboardAdapter.UdpClientHandler handler;

    DatagramSocket socket;
    InetAddress address;

    public UdpClientThread(String comm, String addr, int port, NodeDashboardAdapter.UdpClientHandler handler) {
        super();
        dstAddress = addr;
        dstPort = port;
        command = comm;
        this.handler = handler;
    }



    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        Log.d("DEBUG", "sendState: "+state);
        handler.sendMessage(
                Message.obtain(handler,
                        NodeDashboardAdapter.UdpClientHandler.UPDATE_STATE, state));
    }

    @Override
    public void run() {
        //sendState("connecting...");

        running = true;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(dstAddress);

            // send request
            byte[] buf1 = new byte[256];
            byte[] buf = (command).getBytes();
            DatagramPacket packet =
                    new DatagramPacket(buf, buf.length, address, dstPort);
            socket.send(packet);

            //sendState("connected "+command );

            // get response
            packet = new DatagramPacket(buf1, buf1.length);


            socket.receive(packet);
            String line = new String(packet.getData(), 0, packet.getLength());

            handler.sendMessage(
                    Message.obtain(handler, NodeDashboardAdapter.UdpClientHandler.UPDATE_MSG, line));

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                handler.sendEmptyMessage(NodeDashboardAdapter.UdpClientHandler.UPDATE_END);
            }
        }

    }
}