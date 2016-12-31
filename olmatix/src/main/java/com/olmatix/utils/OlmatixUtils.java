package com.olmatix.utils;

import android.location.Criteria;
import android.text.format.DateFormat;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created              : Rahman on 12/16/2016.
 * Date Created         : 12/16/2016 / 4:30 PM.
 * ===================================================
 * Package              : com.olmatix.utils.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2016 Indogamers.
 */
public class OlmatixUtils {
    private static Criteria criteria;

    public static final int OLMATIX_PERMISSIONS_ACCESS_COARSE_LOCATION = 18;
    public static final long POSITION_UPDATE_INTERVAL = 10 * 1000;//5 seconds
    public static final long POSITION_UPDATE_MIN_DIST = 25;
    public static DecimalFormat gpsDecimalFormat = new DecimalFormat("#.######");

    public static String getTimeAgo(Calendar ref) {
        Calendar now = Calendar.getInstance();

        long milliseconds1 = ref.getTimeInMillis();
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

        long diffSeconds = diff / 1000;
        //Log.d("DEBUG", "getTimeAgo: " + diffSeconds);
        return getScaledTime(diffSeconds) + " ago";
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
        long secondi = typicalOnDurationMsec;
        if (secondi < 60)
            return "" + secondi + " sec.";
        long diffMinutes = secondi / 60;
        secondi = secondi % 60;//resto
        if (diffMinutes < 120)
            return "" + diffMinutes + " minute & " + secondi + " second";
        long diffHours = diffMinutes / (60);
        diffMinutes = diffMinutes % 60;
        return "" + diffHours + " hours " + diffMinutes + " minute";
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

    public static Criteria getGeoCriteria() {

        if (criteria == null)
            criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

}
