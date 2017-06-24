package com.olmatix.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.utils.Connection;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by USER on 02/06/2017.
 */

public class CameraActivity extends AppCompatActivity implements IVLCVout.Callback  {


    private String mFilePath;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    TextView mNicename, mMode;
    static ProgressDialog progressDialog;
    Button hd, sd, ld;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    String olmatixgtwID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();
        mNicename = (TextView) findViewById(R.id.fwname);
        hd = (Button) findViewById(R.id.hd);
        sd = (Button) findViewById(R.id.sd);
        ld = (Button) findViewById(R.id.ld);
        mMode = (TextView) findViewById(R.id.mode);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        olmatixgtwID = "OlmatixGtw-"+sharedPref.getString("CamGateway", "xxxx");

        Intent i = getIntent();
        String path = i.getStringExtra("nodeid");
        String nicename = i.getStringExtra("nice_name");
        mNicename.setText(nicename);
        mFilePath = "rtmp://103.43.47.61/live/"+path;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading your CCTV view, please wait..");
        progressDialog.show();
        setupToolbar();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = "true";
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
        } else {
            Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("addNode");
            intent.putExtra("Connect", "con");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        //mMode.setText("HD");

        hd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilePath = "rtmp://103.43.47.61/live/"+path;
                Toast.makeText(getApplicationContext(), "Switching to HD", Toast.LENGTH_LONG).show();

                mMode.setText("HD");

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = "true";
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
                } else {
                    Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("addNode");
                    intent.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                createPlayer(mFilePath);
                hd.setEnabled(false);
                sd.setEnabled(true);
                ld.setEnabled(true);
                progressDialog.setMessage("Loading High Definition view, please wait..");
            }
        });

        sd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilePath = "rtmp://103.43.47.61/live/"+path+480;
                Toast.makeText(getApplicationContext(), "Switching to SD", Toast.LENGTH_LONG).show();

                mMode.setText("480p");

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = "480";
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
                } else {
                    Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("addNode");
                    intent.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

                createPlayer(mFilePath);
                hd.setEnabled(true);
                sd.setEnabled(false);
                ld.setEnabled(true);
                progressDialog.setMessage("Loading Standar Definition (480p) view, please wait..");
            }
        });

        ld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilePath = "rtmp://103.43.47.61/live/"+path+360;
                Toast.makeText(getApplicationContext(), "Switching to LD", Toast.LENGTH_LONG).show();

                mMode.setText("360p");

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = "360";
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
                } else {
                    Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("addNode");
                    intent.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                createPlayer(mFilePath);
                hd.setEnabled(true);
                sd.setEnabled(true);
                ld.setEnabled(false);
                progressDialog.setMessage("Loading Low Definition (360p) view, please wait..");
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon action bar is clicked; go to parent activity
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    /*************
     * Surface
     *************/
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(holder == null || mSurface == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = "true";
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
        } else {
            Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("addNode");
            intent.putExtra("Connect", "con");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
        createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = "false";
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
        } else {
            Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("addNode");
            intent.putExtra("Connect", "con");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = "false";
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
        } else {
            Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("addNode");
            intent.putExtra("Connect", "con");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }
    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private void createPlayer(String media) {
        releasePlayer();

        try {
            if (media.length() > 0) {
                /*Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();*/
            }

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("--http-reconnect");
            options.add("--network-caching="+6*1000);
            libvlc = new LibVLC(options);

            //libvlc.setOnHardwareAccelerationError(this);
            //holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();


        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
            Log.d("DEBUG", "createPlayer: "+e);
        }

        /*mSurface.getRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    Float p = event.getX()/size.x;
                    Long pos = (long) (mMediaPlayer.getLength() / p);
                    Log.d("DEBUG", "seek to "+p+" / "+pos+" state is "+mMediaPlayer.getPlayerState());
                    if (mMediaPlayer.isSeekable()) {
                        //mLibVLC.setTime( pos );
                        mMediaPlayer.setPosition(p);
                    } else {
                        Log.w("DEBUG", "Non-seekable input");
                    }
                }

                return true;
            }
        });*/
    }

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<CameraActivity> mOwner;

        public MyPlayerListener(CameraActivity owner) {
            mOwner = new WeakReference<CameraActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            CameraActivity player = mOwner.get();
            //Log.d("DEBUG", "Player EVENT");
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d("DEBUG", "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    Log.d("DEBUG", "Media Player Error, re-try");
                    //player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                    progressDialog.hide();
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    progressDialog.show();
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        // Handle errors with hardware acceleration
        Log.e("DEBUG", "Error with hardware acceleration");
        this.releasePlayer();
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }

    private void setupToolbar() {
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>OLMATIX </font>"));

    }


}
