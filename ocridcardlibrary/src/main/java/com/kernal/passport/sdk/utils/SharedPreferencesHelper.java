package com.kernal.passport.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesHelper {
    private static String name = "config";
    /**
     *  Get SharedPreferences real case object
     * @param context
     * @return
     */

    public static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     *Save a value like Boolean type.
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putBoolean(Context context, String key, Boolean value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * Save a value like int type.
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putInt(Context context, String key, int value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        return editor.commit();
    }


    /**
     * Save a value like float type.
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putFloat(Context context, String key, float value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * Save a value like long type
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putLong(Context context, String key, long value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * Save a value like String type.
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putString(Context context, String key, String value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

   

    /**
     *Get String value
     * @param context
     * @param key
     *            Name
     * @param defValue
     *           Default value
     * @return
     */
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getString(key, defValue);
    }

    /**
     * Get int value
     * 
     * @param context
     * @param key
     *            Name
     * @param defValue
     *           Default value
     * @return
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getInt(key, defValue);
    }

    /**
     *  Get float value
     * 
     * @param context
     * @param key
     *            Name
     * @param defValue
     *           Default value
     * @return
     */
    public static float getFloat(Context context, String key, Float defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getFloat(key, defValue);
    }

    /**
     *  Get boolean value
     * 
     * @param context
     * @param key
     *            Name
     * @param defValue
     *             Default value
     * @return
     */
    public static boolean getBoolean(Context context, String key,
            Boolean defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getBoolean(key, defValue);
    }

    /**
     * Get long value
     * 
     * @param context
     * @param key
     *            Name
     * @param defValue
     *             Default value
     * @return
     */
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getLong(key, defValue);
    }

    /**
     * Delete content corresponding to Key
     * 
     * @param context
     * @param key
     *            Name
     * @param defValue
     *            Default value
     * @return
     */
    public static boolean remove(Context context, String key) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.remove(key);
        return editor.commit();
    }

    /**
     *  Delete all the values in SharedPreferences.
     * 
     * @param context
     * @return
     */
    public static boolean clear(Context context) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.clear();
        return editor.commit();
    }
}
