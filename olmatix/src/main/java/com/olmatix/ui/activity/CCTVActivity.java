package com.olmatix.ui.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.olmatix.adapter.GridViewAdapter;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.utils.Connection;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by USER on 02/06/2017.
 */

public class CCTVActivity extends AppCompatActivity implements IVLCVout.Callback   {


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
    Button hd, sd, ld,sc;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    String olmatixgtwID;
    File pathFile;
    String pathThumb;
    RelativeLayout camView;
    ImageView camera_image;
    CheckBox local, cloud;
    String path, date, nicename, ipaddres, nodeid;
    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    public static final String TAG_IMAGE_URL = "image";
    public static final String TAG_NAME = "name";
    GridView gridView;
    ProgressBar loading;
    TextView inputDate, empty;
    String dd, mm, yy;
    DatePickerDialog datepickerdialog;
    Boolean recordplaying;
    Media m;
    ImageButton download;
    String currentUrlView ="";

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mChange = intent.getStringExtra("imagesUrl");
            //Log.d(TAG, "onReceive: ");
            if (mChange==null){
                mChange ="0";
            } else {
                releasePlayer();
                String substr = mChange.substring(mChange.length() - 3);
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = path+";false;"+ipaddres;
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
                    Intent intent1 = new Intent("addNode");
                    intent1.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
                }

                if (substr.equals("jpg")){
                    mSurface.setVisibility(View.GONE);
                    new DownloadImageTask((ImageView) findViewById(R.id.camera_image))
                            .execute(mChange);
                    recordplaying = false;
                    hd.setEnabled(true);
                    sd.setEnabled(true);
                    ld.setEnabled(true);
                    currentUrlView = mChange;
                } else {
                    camera_image.setVisibility(View.GONE);
                    mSurface.setVisibility(View.VISIBLE);
                    mFilePath = mChange;
                    createPlayer(mFilePath);
                    progressDialog.setMessage("Loading recording, please wait..");
                    progressDialog.show();
                    recordplaying=true;
                    hd.setEnabled(true);
                    sd.setEnabled(true);
                    ld.setEnabled(true);
                    currentUrlView = mChange;
                }
            }

        }
    };

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
            releasePlayer();
            progressDialog.setMessage("Loading image, please wait..");
            progressDialog.show();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            progressDialog.hide();
            camera_image.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            //getSupportActionBar().hide();
        }
        else {

            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
            //getSupportActionBar().show();

        }
        setContentView(R.layout.activity_camera);

        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File mediaDir = new File(pictureFolder, "Olmatix");
        if (!mediaDir.exists()){
            mediaDir.mkdir();
        }
        recordplaying = false;

        pathFile = pictureFolder;
        pathThumb = pathFile+"/Olmatix/";

        camView = (RelativeLayout) findViewById(R.id.relative);
        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();
        camera_image = (ImageView) findViewById(R.id.camera_image);
        mNicename = (TextView) findViewById(R.id.fwname);
        hd = (Button) findViewById(R.id.hd);
        sd = (Button) findViewById(R.id.sd);
        ld = (Button) findViewById(R.id.ld);
        sc = (Button) findViewById(R.id.sc);
        gridView = (GridView) findViewById(R.id.grid);
        loading = (ProgressBar) findViewById(R.id.pbProcessing);
        inputDate = (TextView) findViewById(R.id.inputDate);
        empty = (TextView) findViewById(R.id.empty);
        download = (ImageButton) findViewById(R.id.download);

        mMode = (TextView) findViewById(R.id.mode);
        local = (CheckBox) findViewById(R.id.localrecord);
        cloud = (CheckBox) findViewById(R.id.cloudrecord);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //olmatixgtwID = "OlmatixGtw-"+sharedPref.getString("CamGateway", "xxxx");

        Intent i = getIntent();
        nodeid = i.getStringExtra("nodeid");
        nicename = i.getStringExtra("nice_name");
        path = nodeid+"-"+nicename;
        ipaddres = i.getStringExtra("ip");
        mNicename.setText(nicename);
        mFilePath = "rtmp://103.43.47.61/live/"+path;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading your CCTV view, please wait..");
        progressDialog.show();
        olmatixgtwID = nodeid;
        populatedate();

        setupToolbar();
        //initload();
        new GetRecord().execute();

        images = new ArrayList<>();
        names = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        int mYear =  now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear,mMonth,mDay);

        hd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_image.setVisibility(View.GONE);
                mSurface.setVisibility(View.VISIBLE);
                mFilePath = "rtmp://103.43.47.61/live/"+path;
                Toast.makeText(getApplicationContext(), "Switching to HD", Toast.LENGTH_LONG).show();

                mMode.setText("HD");
                Log.d("DEBUG", "Start Streaming Click: ");
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = path+";1280;"+ipaddres;
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
                progressDialog.show();

            }
        });
        sd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_image.setVisibility(View.GONE);
                mSurface.setVisibility(View.VISIBLE);
                mFilePath = "rtmp://103.43.47.61/live/"+path;
                Toast.makeText(getApplicationContext(), "Switching to SD", Toast.LENGTH_LONG).show();

                mMode.setText("480p");

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = path+";480;"+ipaddres;
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
                progressDialog.show();
            }
        });
        ld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_image.setVisibility(View.GONE);
                mSurface.setVisibility(View.VISIBLE);
                mFilePath = "rtmp://103.43.47.61/live/"+path;
                Toast.makeText(getApplicationContext(), "Switching to LD", Toast.LENGTH_LONG).show();

                mMode.setText("360p");

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);
                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = path+";360;"+ipaddres;
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
                progressDialog.show();

            }
        });
        sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
                String mPath = pathThumb + "/" + now + ".jpg";

                //takeScreenshot(camView,mPath);
                //new GetRecord().execute();
                //loading.setVisibility(View.VISIBLE);
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    setSize(mVideoWidth, mVideoHeight);
                    //getSupportActionBar().hide();
                }
                else {
                    //getSupportActionBar().show();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    setSize(mVideoWidth, mVideoHeight);
                    //getSupportActionBar().hide();
                }
            }
        });

        mMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    setSize(mVideoWidth, mVideoHeight);
                    //getSupportActionBar().hide();
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    getSupportActionBar().show();
                    setSize(mVideoWidth, mVideoHeight);
                    //getSupportActionBar().hide();
                }
            }
        });

        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datepickerdialog.show();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!currentUrlView.equals("")) {
                    Toast.makeText(getApplicationContext(), "Download recording to Phone", Toast.LENGTH_SHORT).show();
                    new DownloadFileFromURL().execute(currentUrlView);
                    /*try {
                        takeImgVideo(currentUrlView);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }*/
                } else {
                    Toast.makeText(getApplicationContext(), "Please select recording file first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);

                if (mStatusServer) {
                    String payload = null;
                    String topic = "devices/" + olmatixgtwID + "/value";
                    if (cloud.isChecked()) {
                        payload = path + ";cloudsr;" + ipaddres;
                    } else {
                        payload = path + ";cloudsp;" + ipaddres;
                    }
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
        });

        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mStatusServer = sharedPref.getBoolean("conStatus", false);

                if (mStatusServer) {
                    String topic = "devices/" + olmatixgtwID + "/value";
                    String payload = null;
                    if (local.isChecked()) {
                        payload = path + ";localsr;" + ipaddres;
                    } else {
                        payload = path + ";localsp;" + ipaddres;
                    }
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
        });

    }

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datepickerdialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String daymonth = String.valueOf(dayOfMonth);
                        String mMonth = String.valueOf(++month);

                        if (daymonth.length()==1){
                            daymonth = "0"+dayOfMonth;
                        } else {
                            daymonth = String.valueOf(dayOfMonth);
                        }
                        if (mMonth.length()==1){
                            mMonth = "0"+month;
                        } else {
                            mMonth = String.valueOf(month);
                        }

                        date = year + "-" + mMonth + "-" + daymonth;
                        inputDate.setText(date);
                        gridView.setVisibility(View.GONE);
                        new GetRecord().execute();
                    }
                }, year, month, dayOfMonth);
    }

    private void populatedate() {
        Date tgl = new Date();
        SimpleDateFormat fdd = new SimpleDateFormat("dd");
        dd = String.valueOf(fdd.format(tgl));

        SimpleDateFormat fmm = new SimpleDateFormat("MM");
        mm = String.valueOf(fmm.format(tgl));

        SimpleDateFormat fyy = new SimpleDateFormat("yyyy");
        yy = String.valueOf(fyy.format(tgl));

        date = yy + "-" + mm + "-" + dd;
        inputDate.setText(date);
        setDatePicker(Integer.parseInt(yy),Integer.parseInt(mm),Integer.parseInt(dd));
    }

    private void initload(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);

        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = path+";1280;"+ipaddres;
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setQos(1);
                message.setRetained(true);
                Connection.getClient().publish(topic, message);
                Log.d("DEBUG", "Start Streaming: "+topic);

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

    private void getData(){
        //Showing a progress dialog while our app fetches the data from url
        final ProgressDialog loading = ProgressDialog.show(this, "Please wait...","Fetching data...",false,false);
        String urlJsonArry = "http://103.43.47.61/rest/list_record.php?date=2017-06-28&stream="+path;
        //Creating a json array request to get the json from our api
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(urlJsonArry,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing the progressdialog on response
                        loading.dismiss();

                        //Displaying our grid
                        showGrid(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding our request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    private void showGrid(JSONArray jsonArray){
        //Looping through all the elements of json array
        for(int i = 0; i<jsonArray.length(); i++){
            //Creating a json object of the current index
            JSONObject obj = null;
            try {
                //getting json object from current index
                obj = jsonArray.getJSONObject(i);

                //getting image url and title from json object
                images.add(obj.getString(TAG_IMAGE_URL));
                names.add(obj.getString(TAG_NAME));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Creating GridViewAdapter Object
        GridViewAdapter gridViewAdapter = new GridViewAdapter(this,images,names);

        //Adding adapter to gridview
        gridView.setAdapter(gridViewAdapter);
        loading.setVisibility(View.GONE);

    }

    private class GetRecord extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
            //loading.setScaleY(4f);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
            String urlJsonArry = "http://103.43.47.61/rest/list_record_new.php?date="+date+"&stream="+path;
            Log.d("DEBUG", "doInBackground: "+urlJsonArry);

            JsonArrayRequest req = new JsonArrayRequest(urlJsonArry,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            Log.d("DEBUG", "jsonArray: "+jsonArray.length());

                            images.clear();
                            names.clear();

                            for(int i = 0; i<jsonArray.length(); i++){
                                //Creating a json object of the current index
                                JSONObject obj = null;
                                try {
                                    //getting json object from current index
                                    obj = jsonArray.getJSONObject(i);

                                    //getting image url and title from json object
                                    images.add(obj.getString(TAG_IMAGE_URL));
                                    names.add(obj.getString(TAG_NAME));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            loading.setVisibility(View.GONE);
                            GridViewAdapter gridViewAdapter = new GridViewAdapter(getApplicationContext(),images,names);
                            //Adding adapter to gridview
                            gridView.setAdapter(gridViewAdapter);
                            gridView.setVisibility(View.VISIBLE);
                            empty.setVisibility(View.GONE);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            loading.setVisibility(View.GONE);
                            empty.setVisibility(View.VISIBLE);
                        }
                    });

            // Adding request to request queue
            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            req.setRetryPolicy(policy);
            requestQueue.add(req);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            progressDialog = new ProgressDialog(CCTVActivity.this);
            progressDialog.setMessage("Loading... Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                File mediaDir = new File(pictureFolder, "Olmatix");
                if (!mediaDir.exists()){
                    mediaDir.mkdir();
                }

                pathFile = pictureFolder;
                pathThumb = pathFile+"/Olmatix/";

                System.out.println("Downloading");
                URL url = new URL(f_url[0]);
                String substr = String.valueOf(url).substring(String.valueOf(url).length() - 12);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file

                OutputStream output = new FileOutputStream(pathThumb+substr);
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }



        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            System.out.println("Downloaded");

            progressDialog.dismiss();
        }

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

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            setSize(mVideoWidth, mVideoHeight);

        }
        else {
            getSupportActionBar().show();
            setSize(mVideoWidth, mVideoHeight);
        }
    }

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

        ViewGroup.LayoutParams lp1 = camera_image.getLayoutParams();
        lp1.width = w;
        lp1.height = h;
        camera_image.setLayoutParams(lp1);
        camera_image.invalidate();


        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();

        Log.d("DEBUG", "setSize: "+w +" H "+h);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG", "Start Streaming Resume: ");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = path+";1280;"+ipaddres;
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
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("loadcontent"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        progressDialog.hide();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = path+";false;"+ipaddres;
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
        progressDialog.hide();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            String topic = "devices/" + olmatixgtwID + "/value";
            String payload = path+";false;"+ipaddres;
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
            //options.add("-vvv"); // verbosity
            options.add("--http-reconnect");
            if (recordplaying) {
                options.add("--network-caching=" + 6 * 1000);
                options.add("--loop");
            }

            //options.add("--sout=#transcode{vcodec=h264,venc=x264}:standard{mux=mp4,dst="+pathThumb+"/test.mp4}");

            libvlc = new LibVLC(this,options);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);

            mMediaPlayer.play();


        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
            Log.d("DEBUG", "createPlayer: "+e);
        }

        mSurface.getRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (recordplaying) {
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        Float p = event.getX() / size.x;
                        Long pos = (long) (mMediaPlayer.getLength() / p);
                        Log.d("DEBUG", "seek to " + p + " / " + pos + " state is " + mMediaPlayer.getPlayerState());
                        int stateplayer = mMediaPlayer.getPlayerState();
                        if (mMediaPlayer.isSeekable()) {
                            //mLibVLC.setTime( pos );
                            mMediaPlayer.setPosition(p);
                            if (stateplayer==3) {
                                mMediaPlayer.pause();
                            } else if (stateplayer==4){
                                mMediaPlayer.play();
                            } else if (stateplayer==6){
                                mMediaPlayer.stop();
                                mMediaPlayer.play();
                            }

                        } else {
                            Log.w("DEBUG", "Non-seekable input");
                        }
                    }
                }

                return true;
            }
        });


    }

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<CCTVActivity> mOwner;

        public MyPlayerListener(CCTVActivity owner) {
            mOwner = new WeakReference<CCTVActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            CCTVActivity player = mOwner.get();
            //Log.d("DEBUG", "Player EVENT");
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d("DEBUG", "MediaPlayerEndReached");
                    mMediaPlayer.play();
                    //player.releasePlayer();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    Log.d("DEBUG", "Media Player Error, re-try");
                    //player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                    progressDialog.hide();
                    break;
                case MediaPlayer.Event.Buffering:
                    //progressDialog.show();
                    break;
                case MediaPlayer.Event.Stopped:
                    if (!recordplaying) {
                        progressDialog.show();
                    } else {
                        //mMediaPlayer.play();
                    }

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

    private void createThumbnail () throws FileNotFoundException {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = pathThumb + "/" + now + ".jpg";

            // create bitmap screen capture
            //View v1 = getWindow().getDecorView().getRootView();
           /* mSurface.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mSurface.getWidth(),mSurface.getHeight(), Bitmap.Config.ARGB_8888);
            mSurface.setDrawingCacheEnabled(false);
            mSurface.buildDrawingCache(true);*/



            Bitmap.Config conf = Bitmap.Config.RGB_565;
            Bitmap image = Bitmap.createBitmap(camView.getWidth(),camView.getHeight(), conf);
            Canvas canvas = new Canvas(image);
            canvas.setBitmap(image);
            Paint backgroundPaint = new Paint();
            backgroundPaint.setARGB(255, 40, 40, 40);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                    backgroundPaint);
            mSurface.draw(canvas);
            Bitmap screen = Bitmap.createBitmap(image, 0, 0, camView.getWidth(),camView.getHeight());
            canvas.setBitmap(null);
            //GameThread.surfaceHolder.unlockCanvasAndPost(canvas);


            File imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            screen.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    public static void takeScreenshot(View view, String filePath) {
        Bitmap bitmap = getBitmapScreenshot(view);

        File imageFile = new File(filePath);
        imageFile.getParentFile().mkdirs();
        try {
            OutputStream fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void takeImgVideo(String filePath) throws Throwable {
        Bitmap bitmap = retriveVideoFrameFromVideo(filePath);
        String mPath = pathThumb + "/" + "Olmatix.jpg";
        File imageFile = new File(mPath);
        imageFile.getParentFile().mkdirs();
        try {
            OutputStream fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath)
            throws Throwable
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            Log.d("DEBUG", "retriveVideoFrameFromVideo: "+videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        }
        finally
        {
            if (mediaMetadataRetriever != null)
            {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapScreenshot(View view) {
        view.setBackgroundColor(Color.WHITE);
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
        view.layout((int)view.getX(), (int)view.getY(), (int)view.getX() + view.getMeasuredWidth(), (int)view.getY() + view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        Canvas c = new Canvas(bitmap);
        view.draw(c);
        c.drawBitmap(bitmap, 0, 0, null);

        return bitmap;
    }



}
