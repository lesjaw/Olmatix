package com.olmatix.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

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
    Button hd, sd, ld,sc;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    String olmatixgtwID;
    File pathFile;
    String pathThumb;
    RelativeLayout camView;
    ImageView camera_image;
    CheckBox local, cloud;
    String path;
    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    public static final String TAG_IMAGE_URL = "image";
    public static final String TAG_NAME = "name";
    GridView gridView;
    public ProgressBar loading;


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
            getSupportActionBar().hide();
        }
        else {

            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
            getSupportActionBar().show();

        }
        setContentView(R.layout.activity_camera);

        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File mediaDir = new File(pictureFolder, "Olmatix");
        if (!mediaDir.exists()){
            mediaDir.mkdir();
        }

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
        //listFile = (ListView)findViewById(R.id.listRecord);

        mMode = (TextView) findViewById(R.id.mode);
        local = (CheckBox) findViewById(R.id.localrecord);
        cloud = (CheckBox) findViewById(R.id.cloudrecord);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        olmatixgtwID = "OlmatixGtw-"+sharedPref.getString("CamGateway", "xxxx");

        Intent i = getIntent();
        path = i.getStringExtra("nodeid");
        String nicename = i.getStringExtra("nice_name");
        mNicename.setText(nicename);
        mFilePath = "rtmp://103.43.47.61/live/"+path;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading your CCTV view, please wait..");
        progressDialog.show();
        setupToolbar();
        initload();
        new GetRecord().execute();

        images = new ArrayList<>();
        names = new ArrayList<>();

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

        sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
                String mPath = pathThumb + "/" + now + ".jpg";

                takeScreenshot(camView,mPath);
                //new GetRecord().execute();
                loading.setVisibility(View.VISIBLE);

                //getData();
            }
        });

    }

    private void initload(){
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
            //Toast.makeText(CameraActivity.this,"Downloading recording",Toast.LENGTH_LONG).show();
            loading.setVisibility(View.VISIBLE);
            //loading.setScaleY(4f);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
            String urlJsonArry = "http://103.43.47.61/rest/list_record.php?date=2017-06-28&stream="+path;

            JsonArrayRequest req = new JsonArrayRequest(urlJsonArry,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            Log.d("DEBUG", "jsonArray: "+jsonArray.length());

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
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(CameraActivity.this, "Unable to fetch data: "
                                    + volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("DEBUG", "Unable to fetch data: "+ volleyError.getMessage());
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
            /*ListAdapter adapter = new SimpleAdapter(this, contactList,
                    R.layout.list_item, new String[]{ "email","mobile"},
                    new int[]{R.id.email, R.id.mobile});
            lv.setAdapter(adapter);*/
            loading.setVisibility(View.GONE);
            GridViewAdapter gridViewAdapter = new GridViewAdapter(getApplicationContext(),images,names);
            //Adding adapter to gridview
            gridView.setAdapter(gridViewAdapter);
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
        }
        else {
            getSupportActionBar().show();

        }
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
            //options.add("-vvv"); // verbosity
            options.add("--http-reconnect");
            //options.add("--network-caching="+6*1000);

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

            Media m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();


        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
            Log.d("DEBUG", "createPlayer: "+e);
        }


    }

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private class MyPlayerListener implements MediaPlayer.EventListener {
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
