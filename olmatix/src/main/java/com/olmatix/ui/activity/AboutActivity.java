package com.olmatix.ui.activity;

/**
 * Created by Lesjaw on 03/12/2016.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.olmatix.database.dbNode;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    private dbNode dbnode;
    public static dbNodeRepo mDbNodeRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbNodeRepo = new dbNodeRepo(getApplicationContext());
        dbnode = new dbNode();

        setupToolbar();

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.olmatixmed)
                .addItem(descriptionOlmatixElement())
                .setDescription("We connect everything")
                .addItem(versionOlmatixElement())
                .addGroup("Connect with us")
                .addEmail("info@olmatix.com")
                .addWebsite("http://olmatix.com/")
                .addFacebook("lesjaw")
                .addYoutube("UCWrzb5x0XqFk4vVr8aI9EtQ")
                .addPlayStore("com.olmatix.lesjaw.olmatix")
                .addInstagram("lesjaw")
                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
    }

    Element descriptionOlmatixElement(){
        Element olmatixElement = new Element();
        final String descriptionText = ("Olmatix is a Home Automation System, We have almost complete products to make " +
                "your home smart, energy efficient, safe and secure");
        olmatixElement.setTitle(descriptionText);
        olmatixElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogLog();
            }
        });
        return olmatixElement;
    }

    private void showAlertDialogLog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(AboutActivity.this);
        builderSingle.setIcon(R.drawable.olmatixsmall);
        builderSingle.setTitle("Log Alarm Receiver");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AboutActivity.this, R.layout.list_log_alarm);
        arrayAdapter.addAll(mDbNodeRepo.getLogAlarm());

        builderSingle.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDbNodeRepo.deleteLog();
                dialog.dismiss();
                Toast.makeText(AboutActivity.this, "Log deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(AboutActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    Element versionOlmatixElement(){
        Element olmatixElement = new Element();
        final String descriptionText = ("Olmatix 2.0");
        olmatixElement.setTitle(descriptionText);
        olmatixElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogMqtt();
            }
        });
        return olmatixElement;
    }

    private void showAlertDialogMqtt() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(AboutActivity.this);
        builderSingle.setIcon(R.drawable.olmatixsmall);
        builderSingle.setTitle("Log MQTT Message");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AboutActivity.this, R.layout.list_log_alarm);
        arrayAdapter.addAll(mDbNodeRepo.getLogMqtt());

        builderSingle.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDbNodeRepo.deleteMqtt();
                dialog.dismiss();
                Toast.makeText(AboutActivity.this, "Log deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(AboutActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIcon(R.drawable.ic_copyright_black_24dp);
        copyRightsElement.setColor(R.color.black);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
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
    private void setupToolbar(){
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>About </font>"));

    }
}
