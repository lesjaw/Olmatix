package com.olmatix.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Map;

/**
 * Created              : Rahman on 12/16/2016.
 * Date Created         : 12/16/2016 / 4:30 PM.
 * ===================================================
 * Package              : com.olmatix.utils.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2016 Indogamers.
 */
public class OlmatixUtils {

    public static String getTimeAgo(Calendar ref) {
        Calendar now = Calendar.getInstance();

        long milliseconds1 = ref.getTimeInMillis();
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

        long diffSeconds = diff / 1000;
        Log.d("DEBUG", "getTimeAgo: " + diffSeconds);
        return getScaledTime(diffSeconds) + " Ago";
    }

    public static String getScaledTime(long diffSeconds) {
        if (diffSeconds < 120)
            return "" + diffSeconds + " sec.";
        long diffMinutes = diffSeconds / 60;
        if (diffMinutes < 120)
            return "" + diffMinutes + " min.";
        long diffHours = diffMinutes / (60);
        if (diffHours < 72)
            return "" + diffHours + " hr";

        long diffDays = diffHours / (24);
        return "" + diffDays + " Days";
    }

    public static String getDuration(long typicalOnDurationMsec) {
        long secondi = typicalOnDurationMsec / 1000;
        if (secondi < 60)
            return "" + secondi + " sec.";
        long diffMinutes = secondi / 60;
        secondi = secondi % 60;//resto
        if (diffMinutes < 120)
            return "" + diffMinutes + " minuti e " + secondi + " secondi";
        long diffHours = diffMinutes / (60);
        diffMinutes = diffMinutes % 60;
        return "" + diffHours + " ore e " + diffMinutes + " minuti";
        //return null;
    }

    public static Float celsiusToFahrenheit(float in) {
        return Float.valueOf((9.0f / 5.0f) * in + 32);
    }

    public static Float fahrenheitToCelsius(float fahrenheit) {
        return Float.valueOf((5.0f / 9.0f) * (fahrenheit - 32));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
