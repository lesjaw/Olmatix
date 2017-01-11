package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.olmatix.adapter.WifiListAdapter;
import com.olmatix.lesjaw.olmatix.R;

import java.util.List;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

/**
 * Created by Lesjaw on 11/01/2017.
 */

public class SetupProduct extends AppCompatActivity implements VerticalStepperForm {

    private VerticalStepperFormLayout verticalStepperForm;
    private TextView connectText;
    private static final int CONNECT_TO_PRODUCT = 0;
    private static final int CHOOSE_WIFI = 1;
    private static final int TYPE_WIFI_PASSWORD = 2;

    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;
    WifiListAdapter adapter;
    ListView listWifiDetails;
    List wifiList;
    ListView listtest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_product);

        initializeActivity();
    }

    private void initializeActivity() {

        // Vertical Stepper form vars
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        String[] stepsTitles = getResources().getStringArray(R.array.steps_titles);
        //String[] stepsSubtitles = getResources().getStringArray(R.array.steps_subtitles);

        // Here we find and initialize the form
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(R.id.vertical_stepper_form);
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, stepsTitles, this, this)
                //.stepsSubtitles(stepsSubtitles)
                //.materialDesignInDisabledSteps(true) // false by default
                //.showVerticalLineWhenStepsAreCollapsed(true) // false by default
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
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
        return view;    }

    @Override
    public void onStepOpening(int stepNumber) {

    }

    @Override
    public void sendData() {

    }

    private View createConnectTitleStep() {
        connectText = new TextView(this);
        connectText.setText("Choose WiFi/SSID Product ");

        listtest = new ListView(this);

        String[] statesList = {"listItem 1", "listItem 2", "listItem 3"};
        ArrayAdapter<String> testadap = (new ArrayAdapter<>(this, R.layout.list_item, statesList));

        listtest.setAdapter(testadap);

        int totalHeight = 0;
        for (int i = 0; i < testadap.getCount(); i++) {
            View listItem = testadap.getView(i, null, listtest);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        Log.d("DEBUG", "createConnectTitleStep: "+totalHeight);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = totalHeight + (listtest.getDividerHeight() * (testadap.getCount() - 1));
        Log.d("DEBUG", "createConnectTitleStep: "+params);

        listtest.setLayoutParams(params);
        listtest.requestLayout();

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        receiverWifi = new WifiReceiver();
        listWifiDetails = new ListView(this);

        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        wifiList  = mainWifi.getScanResults();
        Log.d("DEBUG", "createConnectTitleStep: " + wifiList.size());

       /* adapter = new WifiListAdapter(this, wifiList);
        listWifiDetails.setAdapter(adapter);*/

        return listtest;
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
        }
    }

    private View createChooseTitleStep() {
        connectText = new TextView(this);
        connectText.setText("Choose WiFi/SSID Product ");

        return connectText;
    }

    private View createTypePasswordTitleStep() {
        connectText = new TextView(this);
        connectText.setText("Choose WiFi/SSID Product ");

        return connectText;
    }




}
