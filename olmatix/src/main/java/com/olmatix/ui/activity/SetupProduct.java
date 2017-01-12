package com.olmatix.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.olmatix.lesjaw.olmatix.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

import static com.olmatix.lesjaw.olmatix.R.array;
import static com.olmatix.lesjaw.olmatix.R.color;
import static com.olmatix.lesjaw.olmatix.R.id;
import static com.olmatix.lesjaw.olmatix.R.layout;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.setup_product);

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
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), color.white);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), color.colorPrimaryDark);
        String[] stepsTitles = getResources().getStringArray(array.steps_titles);
        //String[] stepsSubtitles = getResources().getStringArray(R.array.steps_subtitles);

        // Here we find and initialize the form
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(id.vertical_stepper_form);
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, stepsTitles, this, this)
                //.stepsSubtitles(stepsSubtitles)
                //.materialDesignInDisabledSteps(true) // false by default
                //.showVerticalLineWhenStepsAreCollapsed(true) // false by default
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
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
        result.setText("Olmatix device will connect to WiFi/SSID "+ssidHome+" with password "+passwordHome+", click OK to proceed");
        new AlertDialog.Builder(this)
                .setTitle("Setup Olmatix product")
                .setMessage("Summary ...")
                .setView(result,(int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendJson();
                        finish();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private View createConnectTitleStep() {
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mainWifi.setWifiEnabled(true);
        listWifiDetails = new ListView(this);

        mainWifi.startScan();
        wifiList = mainWifi.getScanResults();
        Log.d("DEBUG", "createConnectTitleStep: " + wifiList.size());

        listProduct = new ListView(this);
        statesList = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            statesList[i] = String.valueOf(wifiList.get(i).SSID + " || Signal " + wifiList.get(i).level);
            System.out.println(statesList[i]);
        }
        ArrayAdapter<String> testadap = (new ArrayAdapter<>(this, layout.list_item_wifi, statesList));

        listProduct.setAdapter(testadap);

        int totalHeight = 0;
        for (int i = 0; i < testadap.getCount(); i++) {
            View listItem = testadap.getView(i, null, listProduct);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        Log.d("DEBUG", "createConnectTitleStep: " + totalHeight);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = totalHeight + (listProduct.getDividerHeight() * (testadap.getCount() - 1));
        Log.d("DEBUG", "createConnectTitleStep: " + params);

        listProduct.setLayoutParams(params);
        listProduct.requestLayout();

        listProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                String text = (String) listProduct.getItemAtPosition(arg2);
                Log.d("DEBUG", "Selected item: " + text);

                int iend = text.indexOf("|");
                if (iend != -1) {
                    Wifi = text.substring(0, iend);
                    int iend1 = Wifi.indexOf("-");
                    if (iend1 != -1) {
                        Wificut = Wifi.substring(0, iend1);
                        Log.d("DEBUG", "Wifi: " + Wifi);
                    }
                    Password = Wifi.substring(Wifi.lastIndexOf("-") + 1);
                    Log.d("DEBUG", "Password: " + Password);
                    Log.d("DEBUG", "Check If: " + Wificut);

                    arg1.setSelected(true);

                    if (Wificut.equals("Olmatix")) {

                        createAPConfiguration(Wifi, Password,"PSK");
                        textProgres = "Connecting to Olmatix WiFi, if it takes too long connect" +
                                ", please do it manually through your android WiFi setting";
                        progressDialogShow(0);
                    } else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Please pick Olmatix-ID",
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        return listProduct;

    }

    private View createChooseTitleStep() {
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mainWifi.setWifiEnabled(true);
        listWifiDetails = new ListView(this);

        mainWifi.startScan();
        wifiList = mainWifi.getScanResults();
        Log.d("DEBUG", "createConnectTitleStep: " + wifiList.size());

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

        Log.d("DEBUG", "createConnectTitleStep: " + totalHeight);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = totalHeight + (listHome.getDividerHeight() * (testadap.getCount() - 1));
        Log.d("DEBUG", "createConnectTitleStep: " + params);

        listHome.setLayoutParams(params);
        listHome.requestLayout();

        listHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                textHomeWifi = (String) listHome.getItemAtPosition(arg2);
                Log.d("DEBUG", "Selected item: " + textHomeWifi);
                arg1.setSelected(true);
                verticalStepperForm.goToNextStep();
            }
        });
        return listHome;

    }

    private View createTypePasswordTitleStep() {
        wifiText = new EditText(this);
        wifiText.setHint("Type your home WiFi password ");

        return wifiText;
    }

    private boolean checkTitleStep(String Wificut) {
        boolean titleIsCorrect = false;

        Log.d("DEBUG", "checkTitleStep: " + Wificut);

        if (Wificut.equals("Olmatix")) {
            titleIsCorrect = true;

            verticalStepperForm.setActiveStepAsCompleted();
            // Equivalent to: verticalStepperForm.setStepAsCompleted(TITLE_STEP_NUM);

        } else {
            String titleErrorString = getResources().getString(R.string.error_wifi_pick);
            String titleError = getResources().getString(R.string.error_wifi_pick);

            verticalStepperForm.setActiveStepAsUncompleted(titleError);
            // Equivalent to: verticalStepperForm.setStepAsUncompleted(TITLE_STEP_NUM, titleError);

        }

        return titleIsCorrect;
    }

    public void requestInfo() {
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
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );

        requestQueue.add(getRequest);

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

            Snackbar.make(getWindow().getDecorView().getRootView(), "You are connected to "+deviceID+" " +
                    ""+firmware.toUpperCase() +" product", Snackbar.LENGTH_INDEFINITE).show();

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
                        Log.i("VOLLEY", response);
                        if (response.equals("200")) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Setting Olmatix success",
                                    Snackbar.LENGTH_LONG).show();                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
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
                Snackbar.make(getWindow().getDecorView().getRootView(), "You need to choose Home WiFi, Cancel setup!",
                        Snackbar.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode) {
        Log.i("DEBUG", "* SSID request " + networkSSID + " Password " + networkPasskey + " Sec type " + securityMode);

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration.SSID =String.format("\"%s\"", networkSSID.trim());
        Log.d("DEBUG", "createAPConfiguration: "+wifiConfiguration.SSID);

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

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        int networkId = wifi.addNetwork(wifiConfiguration);
        wifi.disconnect();
        wifi.setWifiEnabled(true);
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

                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                String wifiInfo = "";
                WifiInfo info = wifi.getConnectionInfo();
                String ssid = info.getSSID();
                Log.d("DEBUG", "Connected SSID now: " + ssid);
                int iend1 = ssid.indexOf("-");
                if (iend1 != -1) {
                    wifiInfo = ssid.substring(0, iend1);
                    wifiInfo = wifiInfo.replace("\"", "");
                    //Log.d("DEBUG", "Wifi1: " + wifiInfo);
                    if (checkTitleStep(wifiInfo)) {
                        //Log.d("DEBUG", "Wifi2: " + wifiInfo);

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

}
