package com.olmatix.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.SnackbarWrapper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.InstalledNodeModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

import static com.olmatix.lesjaw.olmatix.R.array;
import static com.olmatix.lesjaw.olmatix.R.color;
import static com.olmatix.lesjaw.olmatix.R.id;
import static com.olmatix.lesjaw.olmatix.R.layout;
import static com.olmatix.lesjaw.olmatix.R.string.ssid;

/**
 * Created by Lesjaw on 11/01/2017.
 */

public class SetupProduct extends AppCompatActivity implements VerticalStepperForm {

    private VerticalStepperFormLayout verticalStepperForm;
    private EditText wifiText;
    private static final int CONNECT_TO_PRODUCT = 0;
    private static final int CHOOSE_WIFI = 1;
    private static final int TYPE_WIFI_PASSWORD = 2;

    private WifiManager mainWifi;
    //private WifiReceiver receiverWifi;
    ListView listWifiDetails;
    List<ScanResult> wifiList;
    ListView listProduct, listHome;
    String[] statesList;
    String Wifi = "", Wificut = "",Password, textProgres, deviceID, firmware, version,textHomeWifi;
    private ProgressDialog progressDialog;
    private IntentFilter mIntentFilter;
    private CoordinatorLayout coordinatorLayout;
    private InstalledNodeModel installedNodeModel;
    public static dbNodeRepo mDbNodeRepo;




    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(SetupProduct.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(SetupProduct.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }

    };

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
    private void setupToolbar(){
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Setup Product </font>"));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.setup_product);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        installedNodeModel = new InstalledNodeModel();
        mDbNodeRepo = new dbNodeRepo(getApplicationContext());

        if (canGetLocation() == true) {

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {

            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            showSettingsAlert();

        }
        setupToolbar();


        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        initializeActivity();
    }

    @Override
    protected void onPostResume() {
        registerReceiver(mIntentReceiver, mIntentFilter);

        super.onPostResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mIntentReceiver);
        super.onPause();
    }

    private void initializeActivity() {

        // Vertical Stepper form vars
        String[] stepsTitles = getResources().getStringArray(array.steps_titles);
        //String[] stepsSubtitles = getResources().getStringArray(R.array.steps_subtitles);

        // Here we find and initialize the form
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(id.vertical_stepper_form);
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, stepsTitles, this, this)
                //.stepsSubtitles(stepsSubtitles)
                //.materialDesignInDisabledSteps(true) // false by default
                //.showVerticalLineWhenStepsAreCollapsed(true) // false by default
                .primaryColor(color.light_blue)
                .primaryDarkColor(color.colorPrimaryDark)
                .stepNumberTextColor(color.black)
                .stepSubtitleTextColor(color.white)
                .buttonBackgroundColor(color.bg_button)
                .displayBottomNavigation(true)
                .init();

    }


    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case CONNECT_TO_PRODUCT:
                view = createConnectTitleStep();
                break;
            case CHOOSE_WIFI:
                view = createChooseTitleStep();
                break;
            case TYPE_WIFI_PASSWORD:
                view = createTypePasswordTitleStep();
                break;
        }
        return view;
    }

    @Override
    public void onStepOpening(int stepNumber) {

        switch (stepNumber) {
            case CONNECT_TO_PRODUCT:
                // When this step is open, we check that the title is correct
                checkTitleStep(Wificut);
                break;
            case CHOOSE_WIFI:
                //verticalStepperForm.goToNextStep();
                checkTitleStep(Wificut);
                break;
            case TYPE_WIFI_PASSWORD:
                // As soon as they are open, these two steps are marked as completed because they
                // have default values
                // In this case, the instruction above is equivalent to:
                verticalStepperForm.setActiveStepAsCompleted();
                break;
        }

    }

    @Override
    public void sendData() {
        String passwordHome = wifiText.getText().toString();
        String ssidHome = String.valueOf(textHomeWifi);
        float dpi = this.getResources().getDisplayMetrics().density;
        TextView result = new TextView(this);
        result.setText(getString(R.string.label_setup_sum1)+ssidHome+getString(R.string.label_setup_sum2)+passwordHome+getString(R.string.label_setup_sum3));
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_setup_alert_title)
                .setMessage(R.string.label_setup_sum)
                .setView(result,(int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi))
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendJson();
                        textProgres = getString(R.string.label_setup_sending);
                        progressDialogShow(0);

                    }
                }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private View createConnectTitleStep() {
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //mainWifi.setWifiEnabled(true);
        listWifiDetails = new ListView(this);

        mainWifi.startScan();
        wifiList = mainWifi.getScanResults();

        listProduct = new ListView(this);
        statesList = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            statesList[i] = String.valueOf(wifiList.get(i).SSID + getString(R.string.label_setup_signal) + wifiList.get(i).level);
            System.out.println(statesList[i]);
        }
        ArrayAdapter<String> testadap = new ArrayAdapter<>(this, layout.list_item_wifi, statesList);

        listProduct.setAdapter(testadap);

        int totalHeight = 0;
        for (int i = 0; i < testadap.getCount(); i++) {
            View listItem = testadap.getView(i, null, listProduct);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = totalHeight + (listProduct.getDividerHeight() * (testadap.getCount() - 1));

        listProduct.setLayoutParams(params);
        listProduct.requestLayout();

        listProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String text = (String) listProduct.getItemAtPosition(arg2);
                int iend = text.indexOf("|");
                if (iend != -1) {
                    Wifi = text.substring(0, iend);
                    int iend1 = Wifi.indexOf("-");
                    if (iend1 != -1) {
                        Wificut = Wifi.substring(0, iend1);
                    }
                    Password = Wifi.substring(Wifi.lastIndexOf("-") + 1);
                    arg1.setSelected(true);

                    if (Wificut.equals("Olmatix")) {
                        textProgres = getString(R.string.label_setup_connecting);
                        progressDialogShow(0);

                        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        for( WifiConfiguration i : list ) {
                            wifiManager.removeNetwork(i.networkId);
                            wifiManager.saveConfiguration();
                        }

                        createAPConfiguration(Wifi, Password,"PSK");


                    } else {
                        Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), R.string.labe_setup_pick
                                ,Snackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                    }
                }
            }
        });
        return listProduct;

    }

    private View createChooseTitleStep() {
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
        listWifiDetails = new ListView(this);

        mainWifi.startScan();
        wifiList = mainWifi.getScanResults();

        listHome = new ListView(this);
        statesList = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            statesList[i] = String.valueOf(wifiList.get(i).SSID);
            System.out.println(statesList[i]);
        }
        ArrayAdapter<String> testadap = (new ArrayAdapter<>(this, layout.list_item_wifi, statesList));

        listHome.setAdapter(testadap);

        int totalHeight = 0;
        for (int i = 0; i < testadap.getCount(); i++) {
            View listItem = testadap.getView(i, null, listHome);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = totalHeight + (listHome.getDividerHeight() * (testadap.getCount() - 1));

        listHome.setLayoutParams(params);
        listHome.requestLayout();

        listHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                textHomeWifi = (String) listHome.getItemAtPosition(arg2);
                arg1.setSelected(true);
                verticalStepperForm.goToNextStep();
            }
        });
        return listHome;

    }

    private View createTypePasswordTitleStep() {
        wifiText = new EditText(this);
        wifiText.setHint(R.string.label_setup_pass_home_wifi);

        return wifiText;
    }

    private boolean checkTitleStep(String Wificut) {
        boolean titleIsCorrect = false;

        if (Wificut.equals("Olmatix")) {
            titleIsCorrect = true;

            verticalStepperForm.setActiveStepAsCompleted();
            // Equivalent to: verticalStepperForm.setStepAsCompleted(TITLE_STEP_NUM);

        } else {
            String titleError = getResources().getString(R.string.error_wifi_pick);

            verticalStepperForm.setActiveStepAsUncompleted(titleError);
            // Equivalent to: verticalStepperForm.setStepAsUncompleted(TITLE_STEP_NUM, titleError);

        }

        return titleIsCorrect;
    }

    public void requestInfo() {
        Log.d("DEBUG", "requestInfo: ");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://192.168.1.1/device-info";

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        //Log.d("Response", response.toString());
                        parsingJson(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error requestInfo", String.valueOf(error));
                        requestInfo1();
                    }
                }
        );

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        getRequest.setRetryPolicy(policy);
        requestQueue.add(getRequest);

        //requestQueue.add(getRequest);
    }

    public void requestInfo1() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://192.168.244.1/device-info";

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        //Log.d("Response", response.toString());
                        parsingJson1(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        getRequest.setRetryPolicy(policy);
        requestQueue.add(getRequest);
        //requestQueue.add(getRequest);

    }

    public void parsingJson(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            deviceID = jObject.getString("device_id");
            String firmwareAll = jObject.getString("firmware");
            JSONObject jObject1 = new JSONObject(firmwareAll);
            firmware = jObject1.getString("name");
            version = jObject1.getString("version");

            progressDialogShow(1);

            Snackbar snackbar = Snackbar.make((getWindow().getDecorView()),getString(R.string.label_setup_connect_to)+deviceID+" " +
                            ""+firmware.toUpperCase() +getString(R.string.label_product)
                    ,Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parsingJson1(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            deviceID = jObject.getString("device_id");
            String firmwareAll = jObject.getString("firmware");
            JSONObject jObject1 = new JSONObject(firmwareAll);
            firmware = jObject1.getString("name");
            version = jObject1.getString("version");

            progressDialogShow(1);

            Snackbar snackbar = Snackbar.make((getWindow().getDecorView()),getString(R.string.label_setup_connect_to)+deviceID+" " +
                            ""+firmware.toUpperCase() +getString(R.string.label_product)
                    ,Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendJson() {
        try {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String mUserName = sharedPref.getString("user_name", "olmatix1");
            String mPassword = sharedPref.getString("password", "olmatix");
            String passwordHome = wifiText.getText().toString();
            String ssidHome = String.valueOf(textHomeWifi);
            Log.d("DEBUG", "sendJson: "+mUserName +" | "+mPassword+" | "+textHomeWifi+ " | "+passwordHome);

            if (ssidHome!=null) {

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String URL = "http://192.168.1.1/config";
                JSONObject jsonBody = new JSONObject("{\"name\":\"Olmatix\",\"wifi\": {\"ssid\": \"" + ssidHome + "\",\"password\": " +
                        "\"" + passwordHome + "\"},\"mqtt\": {\"host\": \"cloud.olmatix.com\",\"port\": 1883,\"base_topic\": \"devices/\"," +
                        "\"auth\": true, \"username\": \"" + mUserName + "\",\"password\": \"" + mPassword + "\"},\"ota\": {\"enabled\": false}}");

                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("200")) {

                            Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), R.string.label_setup_success
                                    ,Snackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                                        snackbarView.setBackgroundColor(Color.BLACK);

                            addnode();
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            finish();

                                        }
                                    }, 50000);

                                }
                            });

                            snackbar.show();
                            progressDialogShow(1);


                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                        Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), "Fail, trying another address, please wait.."
                                ,Snackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        sendJson1();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                            // can get more details such as response.headers
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                requestQueue.add(stringRequest);
            } else {
                Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), R.string.label_setup_choose_wifi
                        ,Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addnode(){
        installedNodeModel.setNodesID(deviceID);
        if (mDbNodeRepo.hasObject(installedNodeModel)) {
            final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                    "Checking this Node ID : " + deviceID + ", its exist, please refresh Nodes", TSnackbar.LENGTH_LONG);
            snackbarWrapper.setAction("Olmatix",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "Action",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            snackbarWrapper.show();

        } else {

            installedNodeModel.setNodesID(deviceID);
            installedNodeModel.setFwName("Rename Me!");
            installedNodeModel.setLocalip("localip");
            installedNodeModel.setSignal("0");
            installedNodeModel.setUptime("0");

            mDbNodeRepo.insertDb(installedNodeModel);
        }

                Intent intent = new Intent("addNode");
                intent.putExtra("NodeID", deviceID);
                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);

    }

    public void sendJson1() {
        try {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String mUserName = sharedPref.getString("user_name", "olmatix1");
            String mPassword = sharedPref.getString("password", "olmatix");
            String passwordHome = wifiText.getText().toString();
            String ssidHome = String.valueOf(textHomeWifi);
            Log.d("DEBUG", "sendJson: "+mUserName +" | "+mPassword+" | "+textHomeWifi+ " | "+passwordHome);

            if (ssidHome!=null) {

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String URL = "http://192.168.244.1/config";
                JSONObject jsonBody = new JSONObject("{\"name\":\"Olmatix\",\"wifi\": {\"ssid\": \"" + ssidHome + "\",\"password\": " +
                        "\"" + passwordHome + "\"},\"mqtt\": {\"host\": \"cloud.olmatix.com\",\"port\": 1883,\"base_topic\": \"devices/\"," +
                        "\"auth\": true, \"username\": \"" + mUserName + "\",\"password\": \"" + mPassword + "\"},\"ota\": {\"enabled\": false}}");

                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("200")) {

                            Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), R.string.label_setup_success
                                    ,Snackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.show();
                            progressDialogShow(0);

                            finish();

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                        Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), "Failed setup devices after 2 tries.."
                                ,Snackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
                        finish();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                            // can get more details such as response.headers
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                requestQueue.add(stringRequest);
            } else {
                Snackbar snackbar = Snackbar.make((getWindow().getDecorView()), R.string.label_setup_choose_wifi
                        ,Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode) {

        Log.d("DEBUG", "createAPConfiguration: ");

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiConfiguration.SSID =String.format("\"%s\"", networkSSID.trim());

            wifiConfiguration.preSharedKey = String.format("\"%s\"", networkPasskey.trim());
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);


        int networkId = wifi.addNetwork(wifiConfiguration);

        /*if(mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(false);
        }
        wifi.setWifiEnabled(true);*/
        wifi.disconnect();
        wifi.enableNetwork(networkId, true);
        wifi.reconnect();

        return wifiConfiguration;
    }

    private void progressDialogShow (int what){
        if (what==0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(textProgres);
            progressDialog.show();

        } else {
            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.d("DEBUG", "progressDialogStop: ");
            }
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

                final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                String wifiInfo = "";
                WifiInfo info = wifi.getConnectionInfo();
                String ssid = info.getSSID();
                int iend1 = ssid.indexOf("-");
                if (iend1 != -1) {
                    wifiInfo = ssid.substring(0, iend1);
                    wifiInfo = wifiInfo.replace("\"", "");
                    if (checkTitleStep(wifiInfo)) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                verticalStepperForm.goToStep(2,true);
                                requestInfo();
                            }
                        }, 10000);

                    }
                }
            }

        }
    };

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm;
        boolean gps_enabled = false;
        boolean network_enabled = false;

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Location Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please, enable location setting to read any SSID/WiFi, after you enable it go " +
                "back to setup product setting");

        // On pressing Settings button
        alertDialog.setPositiveButton(
                getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        alertDialog.show();
    }

}
