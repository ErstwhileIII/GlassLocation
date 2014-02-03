package com.jbirdvegas.glassLocation;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jbird on 2/2/14.
 */
public class PrefsHelper {
    public static final String HOME_LAT = "home_lat";
    public static final String HOME_LONG = "home_long";
    private static final String TAG = PrefsHelper.class.getSimpleName();
    private static final String MOST_RECENT_LAT = "most_recent_lat";
    private static final String MOST_RECENT_LONG = "most_recent_long";
    private static final String MOST_RECENT_ACCURACY = "most_recent_accuracy";

    public static double getHomeLat(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(HOME_LAT, -1));
    }

    public static double getHomeLong(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(HOME_LONG, -1));
    }

    public static void setLastLocation(Context context, double lat, double lng, float accuracy) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(MOST_RECENT_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(MOST_RECENT_LONG, Double.doubleToRawLongBits(lng));
        editor.putFloat(MOST_RECENT_ACCURACY, accuracy);
        editor.commit();
    }

    public static double getLastLat(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(MOST_RECENT_LAT, -1));
    }

    public static double getLastLong(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(MOST_RECENT_LONG, -1));
    }

    public static double getLastAccuracy(Context context) {
        return getPrefs(context).getFloat(MOST_RECENT_ACCURACY, -1);
    }

    public static void setHome(Context context) {
        setHomeLat(context, getLastLat(context));
        setHomeLong(context, getLastLat(context));
    }

    public static void setHomeLat(Context context, double lat) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(HOME_LAT, Double.doubleToRawLongBits(lat));
        editor.commit();
    }

    public static void setHomeLong(Context context, double lng) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(HOME_LONG, Double.doubleToRawLongBits(lng));
        editor.commit();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }
}
