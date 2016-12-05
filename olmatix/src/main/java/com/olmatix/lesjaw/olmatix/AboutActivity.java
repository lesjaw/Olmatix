package com.olmatix.lesjaw.olmatix;

/**
 * Created by Lesjaw on 03/12/2016.
 */

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.olmatixlogo)
                .addItem(descriptionOlmatixElement())
                .setDescription("We connect everything")
                .addItem(new Element().setTitle("Version 2.0"))
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
        final String descriptionText = ("Olmatix is a Home Automation System, We have almost complete product to make your home smart, energy efficient, safe and secure");
        olmatixElement.setTitle(descriptionText);
        return olmatixElement;
    }



    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIcon(R.drawable.ic_copyright_black_24dp);
        copyRightsElement.setColor(ContextCompat.getColor(this, mehdi.sakout.aboutpage.R.color.about_item_icon_color));
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
    }
}
