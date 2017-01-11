package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
    ListAdapter adapter;
    ListView listWifiDetails;
    List wifiList;

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

        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout timeStepContent =
                (LinearLayout) inflater.inflate(R.layout.list_view_wifi, null, false);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        receiverWifi = new WifiReceiver();
        listWifiDetails = new ListView(this);

        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        wifiList  = mainWifi.getScanResults();

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, wifiList);

        listWifiDetails.setAdapter(modeAdapter);


        return connectText;
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
